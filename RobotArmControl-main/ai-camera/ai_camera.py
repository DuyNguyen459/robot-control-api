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
import queue

# ============ IMPORT gRPC MỚI ============
import grpc
import robot_arm_pb2
import robot_arm_pb2_grpc

# ============ CONFIGURATION ============
API_BASE_URL = os.getenv("ROBOT_API_BASE_URL", "http://localhost:8080")
WS_URL = os.getenv("ROBOT_WS_URL", "ws://localhost:8080/ws/robot-control")
# CHÚ Ý: Đã bật mặc định USE_GRPC = True để test với Unity
USE_GRPC = True 
SESSION_API_PATH = os.getenv("SESSION_API_PATH", f"{API_BASE_URL}/api/control-sessions/current")
API_BEARER_TOKEN = os.getenv("ROBOT_API_TOKEN", "")

DEVICE_ID = os.getenv("DEVICE_ID", None)

# Camera control state
camera_active = False
camera_lock = threading.Lock()
device_lock = threading.Lock()

# ============ gRPC CLIENT SETUP ============
grpc_queue = queue.Queue(maxsize=100)
grpc_channel = None
grpc_stub = None

def grpc_generator():
    """Generator liên tục lấy tọa độ từ Queue để Stream sang Unity"""
    while True:
        item = grpc_queue.get()
        if item is None:
            break
        yield item

def start_grpc_client():
    """Chạy gRPC trong một Thread riêng để không làm lag Camera"""
    global grpc_channel, grpc_stub
    try:
        grpc_channel = grpc.insecure_channel('localhost:50051')
        grpc_stub = robot_arm_pb2_grpc.RobotArmStub(grpc_channel)
        print("✓ gRPC Connected to Unity (localhost:50051)")
        # Hàm này sẽ block và liên tục gửi data từ generator sang Unity
        grpc_stub.StreamLandmarks(grpc_generator())
    except Exception as e:
        print(f"✗ gRPC Connection Error: {e}")

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
last_timestamp_ms = 0  

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

# ============ SMOOTH & CONTROLLER ============
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

class RobotHandController:
    def __init__(self):
        self.angles = [0.0] * 6
        self.joint_limits = [(-90, 90), (-45, 45), (-60, 60), (-90, 90), (-90, 90), (0, 45)]
        self.joint_names = ["J0 shoulder_link", "J1 arm_link", "J2 elbow_link", "J3 forearm_link", "J4 wrist_link", "J5 hand_link"]
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
        if handedness == "Right": thumb_open = thumb_dx > 0.03
        elif handedness == "Left": thumb_open = thumb_dx < -0.03
        else: thumb_open = abs(thumb_dx) > 0.05
        if not thumb_open: thumb_open = abs(thumb_dx) > 0.07
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
        return self.palm_x_filter.update(x), self.palm_y_filter.update(y)

    def axis_control(self, value):
        if abs(value) < self.deadzone: return 0.0
        mag = (abs(value) - self.deadzone) / (self.max_offset - self.deadzone)
        return math.copysign(self.clamp(mag, 0.0, 1.0), value)

    def update_angles(self, lm, handedness, dt):
        fingers_count = self.count_fingers(lm, handedness)
        self.update_selected_joint(fingers_count)
        palm_x, palm_y = self.get_palm_center(lm)
        control = self.axis_control(palm_x - 0.5)
        j = self.selected_joint
        self.angles[j] += (control * self.max_speed_deg[j]) * dt
        self.angles[j] = self.clamp(self.angles[j], self.joint_limits[j][0], self.joint_limits[j][1])
        return fingers_count, palm_x, palm_y, control

    def reset_angles(self):
        self.angles = [0.0] * 6

controller = RobotHandController()

# ============ COMMUNICATIONS ============
ws_app = None
ws_connected = False

def send_angles(angles):
    """Gửi dữ liệu sang Unity qua gRPC và Backend qua WebSocket"""
    # 1. GỬI SANG UNITY BẰNG gRPC
    if USE_GRPC and grpc_stub is not None:
        for i, angle in enumerate(angles):
            # SỬA Ở ĐÂY: Các biến id, x, y, z bắt buộc phải viết THƯỜNG
            lm = robot_arm_pb2.Landmark(id=i, x=float(angle), y=0.0, z=0.0)
            try:
                grpc_queue.put_nowait(lm)
            except queue.Full:
                pass # Bỏ qua frame nếu hàng đợi đầy để tránh giật lag
    
    # 2. GỬI SANG BACKEND BẰNG WEBSOCKET
    if ws_app is not None and ws_connected:
        with device_lock: dev = DEVICE_ID
        payload = {"type": "ai_angles", "deviceId": dev, "angles": [round(a, 2) for a in angles]}
        try: ws_app.send(json.dumps(payload))
        except: pass

def send_gripper_command(action):
    if ws_app and ws_connected:
        with device_lock: dev = DEVICE_ID
        payload = {"type": "robot_command", "deviceId": dev, "action": action}
        try: ws_app.send(json.dumps(payload))
        except: pass

# --- Bỏ qua phần WebSocket event listeners (giữ nguyên logic gốc của bạn nhưng làm gọn lại) ---
def on_message(ws, message):
    global camera_active, DEVICE_ID
    try:
        data = json.loads(message)
        if data.get("type") == "session_start":
            with device_lock: DEVICE_ID = str(data.get("deviceId"))
            with camera_lock: camera_active = (data.get("controlMode", "").upper() == "CAMERA")
        elif data.get("type") == "session_end":
            with camera_lock: camera_active = False
            with device_lock: DEVICE_ID = None
        elif data.get("type") == "camera_control":
            cmd = data.get("command", "")
            with camera_lock:
                if cmd == "START": camera_active = True
                elif cmd == "STOP": camera_active = False
    except: pass

def on_error(ws, error): print(f"✗ WS error: {error}")
def on_close(ws, close_status_code, close_msg): 
    global ws_connected
    ws_connected = False
def on_open(ws): 
    global ws_connected
    ws_connected = True

def connect_websocket():
    global ws_app
    try:
        ws_app = websocket.WebSocketApp(WS_URL, on_open=on_open, on_message=on_message, on_error=on_error, on_close=on_close)
        threading.Thread(target=ws_app.run_forever, daemon=True).start()
        time.sleep(2)
        return ws_connected
    except: return False

# ============ DRAW & MAIN ============
def draw_hand(image, landmarks):
    if not landmarks: return image
    h, w = image.shape[:2]
    for lm in landmarks:
        cv2.circle(image, (int(lm.x * w), int(lm.y * h)), 5, (255, 0, 0), -1)
    return image

def main():
    global camera_active, last_timestamp_ms

    # BẬT GRPC TRONG THREAD RIÊNG
    if USE_GRPC:
        threading.Thread(target=start_grpc_client, daemon=True).start()

    # BẬT WEBSOCKET
    connect_websocket()

    # CHO PHÉP CAMERA CHẠY NGAY ĐỂ TEST TRƯỚC (Không cần đợi WS START)
    # Xóa dòng này nếu bạn muốn đợi lệnh START từ Frontend
    with camera_lock:
        camera_active = True 

    cap = None
    prev_time = time.time()
    try:
        while True:
            with camera_lock: is_active = camera_active
            
            if is_active:
                if cap is None:
                    cap = cv2.VideoCapture(0)
                    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                    prev_time = time.time()

                ret, frame = cap.read()
                if not ret: continue

                frame = cv2.flip(frame, 1)
                rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                mp_img = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)
                
                timestamp_ms = int(time.monotonic() * 1000)
                if timestamp_ms <= last_timestamp_ms: timestamp_ms = last_timestamp_ms + 1
                last_timestamp_ms = timestamp_ms

                hand_landmarker.detect_async(mp_img, timestamp_ms)

                now = time.time()
                dt = max(0.001, min(now - prev_time, 0.05))
                prev_time = now

                if latest_landmarks:
                    frame = draw_hand(frame, latest_landmarks)
                    fingers_count, palm_x, palm_y, control = controller.update_angles(latest_landmarks, latest_handedness, dt)
                    
                    # GỌI HÀM GỬI DỮ LIỆU
                    send_angles(controller.angles)
                    
                    cv2.putText(frame, f"Fingers: {fingers_count} | Joint: J{controller.selected_joint}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
                    for i, angle in enumerate(controller.angles):
                        cv2.putText(frame, f"J{i}: {angle:.1f}", (10, 60 + i*25), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 0) if i == controller.selected_joint else (255, 255, 255), 1)

                cv2.imshow("AI Camera Robot Control", frame)
                if cv2.waitKey(1) & 0xFF == ord('q'): break
            else:
                if cap:
                    cap.release()
                    cv2.destroyAllWindows()
                    cap = None
                time.sleep(0.1)
                
    except KeyboardInterrupt: pass
    finally:
        if cap: cap.release()
        cv2.destroyAllWindows()
        hand_landmarker.close()
        if USE_GRPC: grpc_queue.put(None) # Dừng thread gRPC

if __name__ == "__main__":
    main()