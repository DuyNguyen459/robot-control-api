# Robot Control System Frontend (Web)

Đây là web frontend dùng để **quản trị** và **vận hành** hệ thống Robot Arm:

- ADMIN: quản lý cấu trúc nhà máy (Factory/Area/Hub), thiết bị (Device) và người dùng (Users).
- OPERATOR: chọn thiết bị và điều khiển Robot Arm bằng **AI Camera** (webcam + MediaPipe Hands), đồng thời ghi nhận log phiên điều khiển.

Repo tổng (RobotArmControl) có nhiều module (backend Java, Flutter app, Python AI…), còn tài liệu này tập trung vào **web frontend** trong thư mục `robot-control-system-frontend/`.

---

## Tech stack

- React 19 + React Router DOM 7
- Vite 7
- TailwindCSS 3
- Axios (interceptor tự gắn JWT)
- MediaPipe Hands (`@mediapipe/hands`) cho AI Camera

---

## Mục tiêu & hành vi chính

### 1) Đăng nhập / xác thực

- Người dùng đăng nhập bằng `LoginModal` trên trang `/`.
- Khi login thành công, FE lưu các giá trị vào `localStorage`:
	- `token` (JWT access token)
	- `role` (ví dụ: `ADMIN`, `OPERATOR`, `VIEWER`)
	- `username`
	- `email` (nếu backend trả về `authData.email`)
	- `factoryId` (nếu backend trả về)
- Điều hướng sau login dùng `navigate(..., { replace: true })` để tránh bấm Back quay lại màn hình login.
- Logout xóa các key trên và điều hướng về `/`.

Code liên quan:

- UI: `src/components/login/LoginModal.jsx`
- API: `src/api/authApi.js`
- Routing bảo vệ: `src/App.jsx` (`PublicRoute`, `ProtectedRoute`, `RoleProtectedRoute`)

### 2) Khu vực Admin: CRUD dữ liệu hệ thống

Các trang CRUD (ADMIN có quyền quản trị; OPERATOR/VIEWER phụ thuộc backend trả về & UI bảo vệ route):

- Factories: tạo/sửa/xóa/list
- Areas: tạo/sửa/xóa/list theo Factory
- Hubs: tạo/sửa/xóa/list theo Area
- Devices: tạo/sửa/xóa/list theo Hub
- Users: chỉ ADMIN truy cập

Các trang tương ứng:

- `src/pages/admin/factories/FactoriesPage.jsx`
- `src/pages/admin/areas/AreasPage.jsx`
- `src/pages/admin/hubs/HubsPage.jsx`
- `src/pages/admin/devices/DevicesPage.jsx`
- `src/pages/admin/users/UsersPage.jsx` (ADMIN-only)

### 3) OPERATOR: chọn Device → điều khiển bằng AI Camera

Luồng thao tác điển hình:

1. Vào `/admin/devices`, chọn Hub rồi click vào 1 device.
2. FE lưu device đã chọn vào `sessionStorage["robotSession.deviceId"]`.
3. Với OPERATOR, sau khi chọn device sẽ điều hướng sang `/admin/ai-camera`.
4. Trên AI Camera page:
	 - Start Session: mở control session trên backend (device-aware)
	 - Start AI Camera: mở webcam + chạy MediaPipe Hands và bắt đầu gửi góc khớp
	 - Stop AI Camera: dừng webcam/sending
	 - End Session: kết thúc session, đồng thời ingest 1 log và lưu “telemetry snapshot” vào sessionStorage để hiển thị ở Logs

Code liên quan:

- Trang điều khiển: `src/pages/admin/aicamera/AiCameraPage.jsx`
- Hook điều khiển: `src/hooks/useAiCamera.js`
- API session/angles: `src/api/cameraService.js`
- WebSocket helper: `src/services/websocketService.js`

### 4) Logs (Operator)

- Với role OPERATOR, route `/admin/dashboard` hiển thị như trang **Logs**.
- Sau khi end session, FE lưu:
	- `sessionStorage["robotLogs.lastSessionId"]` để biết sessionId cần query log
	- `sessionStorage["robotTelemetry.last"]` để hiển thị bảng “TELEMETRY Details”
- Trang Logs gọi backend để lấy log theo sessionId và **lọc bỏ TELEMETRY spam** (chỉ giữ các loại log có ý nghĩa như `AUDIT`, `COMMAND`, `AI_GESTURE`).

Code liên quan:

- Logs page: `src/pages/admin/dashboard/DashboardPage.jsx`
- Log API: `src/api/logService.js`

---

## Routes hiện có

Routing nằm ở `src/App.jsx`.

### Public

- `/` — Home page + mở `LoginModal` để đăng nhập

### Admin (bảo vệ bằng token)

- `/admin` — Layout admin (có sidebar + header)
- `/admin/dashboard`
	- ADMIN: Dashboard tổng quan (hiện có phần số liệu demo)
	- OPERATOR: Logs + TELEMETRY Details (đọc từ sessionStorage + backend logs)
- `/admin/factories` — CRUD Factories
- `/admin/areas` — CRUD Areas
- `/admin/hubs` — CRUD Hubs
- `/admin/devices` — CRUD Devices + chọn device cho phiên điều khiển
- `/admin/users` — Users (ADMIN-only)
- `/admin/ai-camera` — AI Camera Control (OPERATOR-only)
- `/admin/settings` — Settings (ADMIN-only, hiện là trang stub)

---

## Backend API tích hợp (tổng quan)

Trong dev mode, FE gọi API bằng path tương đối (`/api/...`) và Vite proxy sang backend (xem `vite.config.js`).

### Auth

- `POST /api/auth/login`

### CRUD

- Factories: `/api/factories...`
- Areas: `/api/factories/{factoryId}/areas`, `/api/areas/{areaId}...`
- Hubs: `/api/areas/{areaId}/hubs`, `/api/hubs/{hubId}...`
- Devices: `/api/hubs/{hubId}/devices`, `/api/devices/{deviceId}...`

### Control session / AI Camera

- `POST /api/control-sessions` (start session, có thể kèm `deviceId`)
- `GET /api/control-sessions/current` (status)
- `PATCH /api/control-sessions/current/status` (stop session)
- `POST /api/camera/angles` (gửi angles + deviceId)

### Logs

- `POST /api/logs/ingest` (ingest 1 log entry)
- `GET /api/logs/sessions/{sessionId}?limit=...` (query logs theo session)

---

## WebSocket

- FE có `WebsocketService` + `buildWsUrl()` trong `src/services/websocketService.js`.
- Default WS path: `/ws/robot-control` (có thể override qua env).

---

## Storage keys (FE)

### localStorage

- `token`, `role`, `username`, `email`, `factoryId`

### sessionStorage

- `robotSession.deviceId` — device được chọn để điều khiển
- `robotLogs.lastSessionId` — sessionId mới nhất để trang Logs query
- `robotTelemetry.last` — snapshot hiển thị panel TELEMETRY Details

---

## Cách chạy

Yêu cầu: Node.js + npm.

```bash
cd robot-control-system-frontend
npm install
npm run dev
```

Build production:

```bash
cd robot-control-system-frontend
npm run build
```

Ghi chú Windows/PowerShell: nếu bị chặn `npm.ps1` (execution policy), chạy build bằng `cmd`:

```bat
cmd /c "cd /d <path>\robot-control-system-frontend & npm run build"
```

---

## Cấu hình env (tuỳ chọn)

- `VITE_WS_URL`: set full WebSocket URL (override tất cả)
- `VITE_WS_BASE_URL`: set base url cho WS (kèm path)
- `VITE_AI_CAMERA_SELFIE_MODE`: `1` (default) để mirror như selfie, `0` để tắt

---

## Ghi chú kỹ thuật / hạn chế

- `axiosClient` đang có skeleton refresh-token nhưng wiring chưa hoàn chỉnh (chỉ hoạt động nếu có `refreshToken` trong localStorage và endpoint `/auth/refresh` đúng như kỳ vọng).
- Một số trang Admin dashboard (ADMIN) còn demo/hardcode số liệu.
- Các trang load dữ liệu theo kiểu duyệt cây (Factories → Areas → Hubs → Devices) có thể tạo nhiều request khi dữ liệu lớn.
