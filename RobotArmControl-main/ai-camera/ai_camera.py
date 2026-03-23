import cv2
import mediapipe as mp
import websocket
import json
import math
import urllib.request
import urllib.error
import os
import time
import threading
import numpy as np

# ============ CONFIGURATION ============
# API_BASE_URL = os.getenv("ROBOT_API_BASE_URL", "https://robot-control-system-rmbw.onrender.com")
# WS_URL = os.getenv("ROBOT_WS_URL", "wss://robot-control-system-rmbw.onrender.com/ws/robot-control")
API_BASE_URL = os.getenv("ROBOT_API_BASE_URL", "http://localhost:8080")
WS_URL = os.getenv("ROBOT_WS_URL", "ws://localhost:8080/ws/robot-control")
SESSION_API_PATH = os.getenv("SESSION_API_PATH", f"{API_BASE_URL}/api/control-sessions/current")
API_BEARER_TOKEN = os.getenv("ROBOT_API_TOKEN", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0ZXN0IiwidXNlcklkIjozLCJyb2xlIjoiQURNSU4iLCJlbWFpbCI6InN0cmluZ0BnbWFpbC5jb20iLCJpYXQiOjE3NzM1NTgxNDQsImV4cCI6MTc3MzY0NDU0NH0._fFa8ms5ZB4-65PJ4JukobB91-24EfSdan_o-bd-br3XFjfxzYx7gf90ZgMcZd8H")

# DEVICE_ID will be fetched from API at startup if available; fallback from env
DEVICE_ID = os.getenv("DEVICE_ID", None)

# Camera control state
camera_active = False
camera_lock = threading.Lock()
device_lock = threading.Lock()
stop_event = threading.Event()

# ============ SETUP MEDIAPIPE ============
BaseOptions = mp.tasks.BaseOptions
HandLandmarker = mp.tasks.vision.HandLandmarker
HandLandmarkerOptions = mp.tasks.vision.HandLandmarkerOptions
VisionRunningMode = mp.tasks.vision.RunningMode

MODEL_PATH = "hand_landmarker.task"
if not os.path.exists(MODEL_PATH):
    print("Downloading model...")
    url = "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task"
    urllib.request.urlretrieve(url, MODEL_PATH)

latest_landmarks = None
latest_handedness = None
last_timestamp_ms = 0  # monotonic timestamp for MediaPipe

def result_callback(result, output_image, timestamp_ms):
    global latest_landmarks, latest_handedness
    if result.hand_landmarks:
        latest_landmarks = result.hand_landmarks[0]
        if result.handedness and len(result.handedness) > 0 and len(result.handedness[0]) > 0:
            latest_handedness = result.handedness[0][0].category_name
        else:
            latest_handedness = None
    else:
        latest_landmarks = None
        latest_handedness = None

options = HandLandmarkerOptions(
    base_options=BaseOptions(model_asset_path=MODEL_PATH),
    running_mode=VisionRunningMode.LIVE_STREAM,
    num_hands=1,
    min_hand_detection_confidence=0.6,
    min_tracking_confidence=0.6,
    result_callback=result_callback
)
hand_landmarker = HandLandmarker.create_from_options(options)

# ============ SMOOTH ============
class LowPassValue:
    def __init__(self, alpha=0.25, initial=0.0):
        self.alpha = alpha
        self.value = initial
        self.initialized = False

    def update(self, x):
        if not self.initialized:
            self.value = x
            self.initialized = True
        else:
            self.value = self.alpha * x + (1 - self.alpha) * self.value
        return self.value

# ============ CONTROLLER ============
class RobotHandController:
    def __init__(self):
        self.angles = [0.0] * 6
        self.joint_limits = [
            (-90, 90),
            (-45, 45),
            (-60, 60),
            (-90, 90),
            (-90, 90),
            (0, 45)
        ]
        self.joint_names = [
            "J0 shoulder_link",
            "J1 arm_link",
            "J2 elbow_link",
            "J3 forearm_link",
            "J4 wrist_link",
            "J5 hand_link"
        ]
        self.selected_joint = 0
        self.pending_joint = 0
        self.pending_since = time.time()
        self.mode_hold_seconds = 0.30
        self.max_speed_deg = [120, 90, 90, 100, 100, 100]
        self.deadzone = 0.08
        self.max_offset = 0.35
        self.palm_x_filter = LowPassValue(alpha=0.25, initial=0.5)
        self.palm_y_filter = LowPassValue(alpha=0.25, initial=0.5)

    def clamp(self, value, min_v, max_v):
        return max(min_v, min(max_v, value))

    def count_fingers(self, lm, handedness=None):
        fingers = 0
        if lm[8].y < lm[6].y: fingers += 1
        if lm[12].y < lm[10].y: fingers += 1
        if lm[16].y < lm[14].y: fingers += 1
        if lm[20].y < lm[18].y: fingers += 1
        thumb_dx = lm[4].x - lm[2].x
        if handedness == "Right":
            thumb_open = thumb_dx > 0.03
        elif handedness == "Left":
            thumb_open = thumb_dx < -0.03
        else:
            thumb_open = abs(thumb_dx) > 0.05
        if not thumb_open:
            thumb_open = abs(thumb_dx) > 0.07
        if thumb_open: fingers += 1
        return fingers

    def update_selected_joint(self, fingers_count):
        target_joint = max(0, min(5, fingers_count))
        now = time.time()
        if target_joint != self.pending_joint:
            self.pending_joint = target_joint
            self.pending_since = now
        else:
            if now - self.pending_since >= self.mode_hold_seconds:
                self.selected_joint = self.pending_joint

    def get_palm_center(self, lm):
        ids = [0, 5, 9, 13, 17]
        x = sum(lm[i].x for i in ids) / len(ids)
        y = sum(lm[i].y for i in ids) / len(ids)
        x = self.palm_x_filter.update(x)
        y = self.palm_y_filter.update(y)
        return x, y

    def axis_control(self, value):
        if abs(value) < self.deadzone:
            return 0.0
        mag = (abs(value) - self.deadzone) / (self.max_offset - self.deadzone)
        mag = self.clamp(mag, 0.0, 1.0)
        return math.copysign(mag, value)

    def update_angles(self, lm, handedness, dt):
        fingers_count = self.count_fingers(lm, handedness)
        self.update_selected_joint(fingers_count)
        palm_x, palm_y = self.get_palm_center(lm)
        control_signal = palm_x - 0.5
        control = self.axis_control(control_signal)
        j = self.selected_joint
        speed = control * self.max_speed_deg[j]
        self.angles[j] += speed * dt
        mn, mx = self.joint_limits[j]
        self.angles[j] = self.clamp(self.angles[j], mn, mx)
        return fingers_count, palm_x, palm_y, control

    def reset_angles(self):
        self.angles = [0.0] * 6

controller = RobotHandController()

# Auto gripper action from pinch gesture (thumb tip - index tip)
PINCH_GRAB_THRESHOLD = 0.045
PINCH_RELEASE_THRESHOLD = 0.085
GRIPPER_HOLD_SECONDS = 0.22
GRIPPER_COOLDOWN_SECONDS = 0.80

gripper_state = None
gripper_candidate = None
gripper_candidate_since = 0.0
last_gripper_sent_at = 0.0

# ============ WEBSOCKET ============
ws_app = None
ws_connected = False

def on_message(ws, message):
    global camera_active, DEVICE_ID
    try:
        data = json.loads(message)
        msg_type = data.get("type", "")
        if msg_type == "session_start":
            control_mode = data.get("controlMode", "CAMERA").upper()
            device = data.get("deviceId")
            if device:
                with device_lock:
                    DEVICE_ID = str(device)
                print(f"[WS] session_start received -> deviceId set: {DEVICE_ID}")
            with camera_lock:
                camera_active = (control_mode == "CAMERA")
            print(f"[WS] session_start received (controlMode={control_mode}) -> camera_active={camera_active}")
            return

        if msg_type == "session_end":
            with camera_lock:
                camera_active = False
            with device_lock:
                DEVICE_ID = None
            print("[WS] session_end received -> camera_active=False, deviceId cleared")
            return

        if msg_type == "camera_control":
            command = data.get("command", "")
            with camera_lock:
                if command == "START" and not camera_active:
                    camera_active = True
                    print(">>> AI CAMERA ACTIVATED - Bắt đầu điều khiển!")
                elif command == "STOP" and camera_active:
                    camera_active = False
                    print(">>> AI CAMERA DEACTIVATED - Kết thúc điều khiển")
            return

    except json.JSONDecodeError:
        pass
    except Exception as e:
        print(f"Error processing message: {e}")

def on_error(ws, error):
    print(f"✗ WebSocket error: {error}")

def on_close(ws, close_status_code, close_msg):
    global ws_connected
    ws_connected = False
    print(f"WebSocket disconnected (code: {close_status_code})")

def on_open(ws):
    global ws_connected
    ws_connected = True
    with device_lock:
        dev = DEVICE_ID
    print(f"✓ WebSocket connected. DEVICE_ID (for sends): {dev}")

def connect_websocket():
    global ws_app
    try:
        ws_app = websocket.WebSocketApp(
            WS_URL,
            on_open=on_open,
            on_message=on_message,
            on_error=on_error,
            on_close=on_close
        )
        ws_thread = threading.Thread(target=ws_app.run_forever, daemon=True)
        ws_thread.start()
        time.sleep(2)
        return ws_connected
    except Exception as e:
        print(f"✗ Failed to connect: {e}")
        return False

def send_angles(angles):
    global ws_app
    if ws_app is None or not ws_connected:
        return
    with device_lock:
        dev = DEVICE_ID
    payload = {
        "type": "ai_angles",
        "deviceId": dev,
        "angles": [round(a, 2) for a in angles]
    }
    try:
        ws_app.send(json.dumps(payload))
    except Exception as e:
        print(f"! Error sending angles: {e}")

def send_gripper_command(action):
    global ws_app
    if ws_app is None or not ws_connected:
        return

    normalized = str(action or "").strip().lower()
    if normalized not in ("grab", "release"):
        return

    with device_lock:
        dev = DEVICE_ID

    payload = {
        "type": "robot_command",
        "deviceId": dev,
        "action": normalized
    }
    try:
        ws_app.send(json.dumps(payload))
        print(f"[WS] robot_command sent: {payload}")
    except Exception as e:
        print(f"! Error sending robot_command: {e}")

def get_pinch_distance(lm):
    if not lm or len(lm) < 9:
        return None
    dx = lm[4].x - lm[8].x
    dy = lm[4].y - lm[8].y
    return math.sqrt(dx * dx + dy * dy)

def detect_gripper_action_from_pinch(pinch_distance):
    if pinch_distance is None:
        return None
    if pinch_distance <= PINCH_GRAB_THRESHOLD:
        return "grab"
    if pinch_distance >= PINCH_RELEASE_THRESHOLD:
        return "release"
    return None

def maybe_send_auto_gripper_action(action, now):
    global gripper_state, gripper_candidate, gripper_candidate_since, last_gripper_sent_at

    if action is None:
        return

    if gripper_candidate != action:
        gripper_candidate = action
        gripper_candidate_since = now
        return

    if now - gripper_candidate_since < GRIPPER_HOLD_SECONDS:
        return

    if gripper_state == action:
        return

    if now - last_gripper_sent_at < GRIPPER_COOLDOWN_SECONDS:
        return

    send_gripper_command(action)
    gripper_state = action
    last_gripper_sent_at = now

# ============ HELPER: fetch session from API ============
def fetch_session_from_api(timeout=5, retries=3, backoff=1.0):
    """Call SESSION_API_PATH to get session info (expects ApiResponse with 'data' containing deviceId and controlMode)."""
    global DEVICE_ID
    if API_BEARER_TOKEN:
        print(f"[API] Using bearer token (length={len(API_BEARER_TOKEN)}). URL={SESSION_API_PATH}")
    else:
        print(f"[API] No bearer token set. URL={SESSION_API_PATH}")

    attempt = 0
    while attempt < retries:
        attempt += 1
        try:
            req = urllib.request.Request(SESSION_API_PATH, method="GET")
            if API_BEARER_TOKEN:
                req.add_header("Authorization", f"Bearer {API_BEARER_TOKEN}")
            req.add_header("Accept", "application/json")
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                body = resp.read().decode("utf-8")
                status = getattr(resp, "status", None)
                print(f"[API] HTTP {status} received on attempt {attempt}")
                try:
                    wrapper = json.loads(body)
                except Exception:
                    print(f"[API] Failed to parse JSON body: {body}")
                    wrapper = None

                # try to extract payload from ApiResponse wrapper
                payload = None
                if isinstance(wrapper, dict) and "data" in wrapper:
                    payload = wrapper.get("data")
                elif isinstance(wrapper, dict):
                    payload = wrapper
                else:
                    payload = wrapper

                print(f"[API] response wrapper: {wrapper}")
                if not payload:
                    print(f"[API] no payload/data found in response")
                    return None

                device = payload.get("deviceId")
                control_mode = payload.get("controlMode")
                if device is not None:
                    with device_lock:
                        DEVICE_ID = str(device)
                    print(f"[API] fetched deviceId from session API: {DEVICE_ID}")
                else:
                    print(f"[API] payload has no deviceId: {payload}")

                return {"deviceId": device, "controlMode": control_mode}
        except urllib.error.HTTPError as he:
            body = he.read().decode("utf-8") if hasattr(he, "read") else ""
            print(f"[API] HTTPError {he.code}: {he.reason}. Body: {body}")
            if he.code in (401, 403):
                print("[API] Authorization failed (401/403) - check ROBOT_API_TOKEN and user roles.")
                return None
        except Exception as e:
            print(f"[API] fetch attempt {attempt} error: {e}")
        time.sleep(backoff * attempt)
    print("[API] fetch_session_from_api exhausted retries")
    return None

# ============ DRAW & MAIN ============
def draw_hand(image, landmarks):
    if not landmarks:
        return image
    h, w = image.shape[:2]
    connections = [
        (0, 1), (1, 2), (2, 3), (3, 4),
        (0, 5), (5, 6), (6, 7), (7, 8),
        (0, 9), (9, 10), (10, 11), (11, 12),
        (0, 13), (13, 14), (14, 15), (15, 16),
        (0, 17), (17, 18), (18, 19), (19, 20),
        (5, 9), (9, 13), (13, 17)
    ]
    for c in connections:
        p1, p2 = landmarks[c[0]], landmarks[c[1]]
        cv2.line(
            image,
            (int(p1.x * w), int(p1.y * h)),
            (int(p2.x * w), int(p2.y * h)),
            (0, 255, 0), 2
        )
    for lm in landmarks:
        cv2.circle(image, (int(lm.x * w), int(lm.y * h)), 5, (255, 0, 0), -1)
    return image

def draw_ui(frame, controller, fingers_count, handedness, palm_x, control):
    h, w = frame.shape[:2]
    center_x = int(w * 0.5)
    dz_left = int(w * (0.5 - controller.deadzone))
    dz_right = int(w * (0.5 + controller.deadzone))
    cv2.line(frame, (center_x, 0), (center_x, h), (255, 255, 255), 1)
    cv2.line(frame, (dz_left, 0), (dz_left, h), (100, 100, 255), 1)
    cv2.line(frame, (dz_right, 0), (dz_right, h), (100, 100, 255), 1)
    px = int(palm_x * w)
    cv2.line(frame, (px, 0), (px, h), (0, 255, 255), 2)
    cv2.putText(frame, f"Hand: {handedness or 'Unknown'}", (10, 25),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 0), 2)
    cv2.putText(frame, f"Fingers: {fingers_count}", (10, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 0), 2)
    cv2.putText(frame, f"Selected: {controller.joint_names[controller.selected_joint]}", (10, 80),
                cv2.FONT_HERSHEY_SIMPLEX, 0.58, (0, 255, 255), 2)
    cv2.putText(frame, f"Control: {control:+.2f}", (10, 105),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 200, 255), 2)
    for i, angle in enumerate(controller.angles):
        color = (0, 255, 255) if i == controller.selected_joint else (0, 255, 0)
        cv2.putText(frame, f"{controller.joint_names[i]}: {angle:>7.1f}", (10, 140 + i * 26),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.54, color, 2)
    instructions = [
        "0 ngon -> J0 shoulder_link",
        "1 ngon -> J1 arm_link",
        "2 ngon -> J2 elbow_link",
        "3 ngon -> J3 forearm_link",
        "4 ngon -> J4 wrist_link",
        "5 ngon -> J5 hand_link",
        "Tat ca khop: di tay trai/phai de giam/tang goc",
        "J0 range: -175 .. 175",
        "R = Reset | G = Grab | F = Release | Q = Quit"
    ]
    y0 = h - len(instructions) * 22 - 10
    for i, text in enumerate(instructions):
        cv2.putText(frame, text, (10, y0 + i * 22),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.48, (200, 200, 200), 1)

def main():
    global camera_active, last_timestamp_ms, DEVICE_ID
    print("=" * 72)
    print(" AI Camera - 6 DOF Robot Arm Control")
    print("=" * 72)
    print(f"Server: {API_BASE_URL}")
    print("=" * 72)

    # Try to fetch session/device assignment from API (optional)
    session_info = fetch_session_from_api()
    if session_info:
        if session_info.get("deviceId"):
            with device_lock:
                DEVICE_ID = str(session_info.get("deviceId"))
            print(f"[API] Received deviceId: {DEVICE_ID}")
        else:
            print("[API] No deviceId in session_info")
        cm = (session_info.get("controlMode") or "").upper() if session_info.get("controlMode") else ""
        if cm == "CAMERA":
            with camera_lock:
                camera_active = True
            print(f"[API] controlMode={cm} -> camera_active=True")

    if not connect_websocket():
        print("\n✗ Cannot connect to WebSocket. Exiting.")
        return

    cap = None
    prev_time = time.time()
    try:
        while True:
            with camera_lock:
                is_active = camera_active
            if is_active:
                if cap is None:
                    cap = cv2.VideoCapture(0)
                    if not cap.isOpened():
                        print("✗ Cannot open camera")
                        camera_active = False
                        continue
                    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                    print("✓ Camera started")
                    prev_time = time.time()

                ret, frame = cap.read()
                if not ret:
                    time.sleep(0.01)
                    continue

                frame = cv2.flip(frame, 1)
                rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                mp_img = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)

                timestamp_ms = int(time.monotonic() * 1000)
                if timestamp_ms <= last_timestamp_ms:
                    timestamp_ms = last_timestamp_ms + 1
                last_timestamp_ms = timestamp_ms

                hand_landmarker.detect_async(mp_img, timestamp_ms)

                now = time.time()
                dt = now - prev_time
                prev_time = now
                dt = max(0.001, min(dt, 0.05))

                if latest_landmarks:
                    frame = draw_hand(frame, latest_landmarks)
                    fingers_count, palm_x, palm_y, control = controller.update_angles(
                        latest_landmarks,
                        latest_handedness,
                        dt
                    )
                    send_angles(controller.angles)

                    pinch_distance = get_pinch_distance(latest_landmarks)
                    gripper_action = detect_gripper_action_from_pinch(pinch_distance)
                    maybe_send_auto_gripper_action(gripper_action, now)

                    draw_ui(frame, controller, fingers_count, latest_handedness, palm_x, control)
                else:
                    cv2.putText(frame, "Dua tay vao camera", (10, 30),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
                    for i, angle in enumerate(controller.angles):
                        cv2.putText(frame, f"{controller.joint_names[i]}: {angle:>7.1f}", (10, 70 + i * 28),
                                    cv2.FONT_HERSHEY_SIMPLEX, 0.58, (0, 255, 0), 2)

                cv2.putText(frame, "ACTIVE", (frame.shape[1] - 100, 30),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
                cv2.imshow("AI Camera Robot Control", frame)
                key = cv2.waitKey(1) & 0xFF
                if key == ord('q'):
                    break
                elif key == ord('r'):
                    controller.reset_angles()
                    print("Angles reset.")
                elif key == ord('g'):
                    send_gripper_command("grab")
                elif key == ord('f'):
                    send_gripper_command("release")
            else:
                if cap is not None:
                    cap.release()
                    cv2.destroyAllWindows()
                    cap = None
                    print("Camera stopped. Waiting for START command...")
                wait_frame = 255 * np.ones((200, 500, 3), dtype=np.uint8)
                cv2.putText(wait_frame, "AI Camera - Standby Mode", (50, 80),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 0), 2)
                cv2.putText(wait_frame, "Waiting for START from Frontend...", (50, 130),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (100, 100, 100), 1)
                cv2.putText(wait_frame, "Press Q to quit", (50, 170),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (100, 100, 100), 1)
                cv2.imshow("AI Camera Robot Control", wait_frame)
                key = cv2.waitKey(100) & 0xFF
                if key == ord('q'):
                    break
    except KeyboardInterrupt:
        print("\nInterrupted by user")
    finally:
        if cap is not None:
            cap.release()
        cv2.destroyAllWindows()
        try:
            hand_landmarker.close()
        except:
            pass
        if ws_app:
            ws_app.close()
        print("AI Camera đã dừng.")

if __name__ == "__main__":
    main()