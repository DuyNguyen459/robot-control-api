export function getRole() {
  return String(localStorage.getItem("role") || "").trim().toUpperCase();
}

export function getFactoryId() {
  const raw = localStorage.getItem("factoryId");
  if (raw == null || String(raw).trim() === "") return null;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

export function isAdminRole(role) {
  return String(role || "").trim().toUpperCase() === "ADMIN";
}

export function isOperatorRole(role) {
  return String(role || "").trim().toUpperCase() === "OPERATOR";
}

export function getDefaultAdminPath(role) {
  const r = String(role || "").trim().toUpperCase();
  if (r === "OPERATOR") return "/admin/ai-camera";
  return "/admin/dashboard";
}
