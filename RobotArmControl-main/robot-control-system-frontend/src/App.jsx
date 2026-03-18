import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AdminLayout from "./components/Layout/AdminLayout";
import DashboardPage from "./pages/admin/dashboard/DashboardPage";
import FactoriesPage from "./pages/admin/factories/FactoriesPage";
import AreasPage from "./pages/admin/areas/AreasPage";
import HubsPage from "./pages/admin/hubs/HubsPage";
import DevicesPage from "./pages/admin/devices/DevicesPage";
import UsersPage from "./pages/admin/users/UsersPage";
import AiCameraPage from "./pages/admin/aicamera/AiCameraPage";
import SettingsPage from "./pages/admin/settings/SettingsPage";
import HomePage from "./pages/homepage/HomePage";
import { getDefaultAdminPath, getRole } from "./utils/auth";

function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/" replace />;
}

function RoleProtectedRoute({ allow = [], children }) {
  const token = localStorage.getItem("token");
  const role = getRole();

  if (!token) return <Navigate to="/" replace />;
  if (Array.isArray(allow) && allow.length > 0 && !allow.includes(role)) {
    return <Navigate to={getDefaultAdminPath(role)} replace />;
  }

  return children;
}

function PublicRoute({ children }) {
  const token = localStorage.getItem("token");
  const role = getRole();
  return token ? <Navigate to={getDefaultAdminPath(role)} replace /> : children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <PublicRoute>
              <HomePage />
            </PublicRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route
            index
            element={<Navigate to={getDefaultAdminPath(getRole()).replace("/admin/", "")} replace />}
          />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="factories" element={<FactoriesPage />} />
          <Route path="areas" element={<AreasPage />} />
          <Route path="hubs" element={<HubsPage />} />
          <Route path="devices" element={<DevicesPage />} />
          <Route
            path="users"
            element={
              <RoleProtectedRoute allow={["ADMIN"]}>
                <UsersPage />
              </RoleProtectedRoute>
            }
          />
          <Route
            path="ai-camera"
            element={
              <RoleProtectedRoute allow={["OPERATOR"]}>
                <AiCameraPage />
              </RoleProtectedRoute>
            }
          />
          <Route
            path="settings"
            element={
              <RoleProtectedRoute allow={["ADMIN"]}>
                <SettingsPage />
              </RoleProtectedRoute>
            }
          />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;