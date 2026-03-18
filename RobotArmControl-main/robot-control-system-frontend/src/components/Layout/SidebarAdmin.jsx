import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  DashboardOutlined,
  BankOutlined,
  ApartmentOutlined,
  ClusterOutlined,
  RobotOutlined,
  UserOutlined,
  VideoCameraOutlined,
} from "@ant-design/icons";
import { getRole, isAdminRole, isOperatorRole } from "../../utils/auth";

function parseJwt(token) {
  try {
    const parts = String(token || "").split(".");
    if (parts.length < 2) return null;

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function SidebarAdmin({ collapsed }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const userMenuRef = useRef(null);

  const role = getRole();
  const isAdmin = isAdminRole(role);
  const isOperator = isOperatorRole(role);

  const items = isAdmin
    ? [
        { key: "/admin/dashboard", label: "Dashboard", icon: <DashboardOutlined /> },
        { key: "/admin/factories", label: "Factories", icon: <BankOutlined /> },
        { key: "/admin/areas", label: "Areas", icon: <ApartmentOutlined /> },
        { key: "/admin/hubs", label: "Hubs", icon: <ClusterOutlined /> },
        { key: "/admin/devices", label: "Devices", icon: <RobotOutlined /> },
        { key: "/admin/users", label: "Users", icon: <UserOutlined /> },
      ]
    : isOperator
      ? [
          { key: "/admin/dashboard", label: "Logs", icon: <DashboardOutlined /> },
          { key: "/admin/ai-camera", label: "Control Robot", icon: <VideoCameraOutlined /> },
          { key: "/admin/factories", label: "Factories", icon: <BankOutlined /> },
          { key: "/admin/areas", label: "Areas", icon: <ApartmentOutlined /> },
          { key: "/admin/hubs", label: "Hubs", icon: <ClusterOutlined /> },
          { key: "/admin/devices", label: "Devices", icon: <RobotOutlined /> },
        ]
      : [
          { key: "/admin/dashboard", label: "Dashboard", icon: <DashboardOutlined /> },
          { key: "/admin/factories", label: "Factories", icon: <BankOutlined /> },
          { key: "/admin/areas", label: "Areas", icon: <ApartmentOutlined /> },
          { key: "/admin/hubs", label: "Hubs", icon: <ClusterOutlined /> },
          { key: "/admin/devices", label: "Devices", icon: <RobotOutlined /> },
          { key: "/admin/ai-camera", label: "AI Camera Control", icon: <VideoCameraOutlined /> },
        ];

  const isActive = (path) =>
    location.pathname === path || location.pathname.startsWith(path + "/");

  const username = String(localStorage.getItem("username") || "").trim();
  const displayName = username || "User";
  const avatarText = displayName ? displayName.slice(0, 1).toUpperCase() : "U";

  const storedEmail = String(localStorage.getItem("email") || "").trim();
  const token = localStorage.getItem("token");
  const claims = parseJwt(token);
  const emailFromClaims =
    (claims && (claims.email || claims.userEmail || claims.mail || claims.upn)) || "";
  const email =
    storedEmail ||
    (typeof emailFromClaims === "string" && emailFromClaims.includes("@")
      ? emailFromClaims
      : typeof claims?.sub === "string" && claims.sub.includes("@")
        ? claims.sub
        : "");

  useEffect(() => {
    if (!userMenuOpen) return;

    const onDocMouseDown = (e) => {
      const el = userMenuRef.current;
      if (!el) return;
      if (el.contains(e.target)) return;
      setUserMenuOpen(false);
    };

    document.addEventListener("mousedown", onDocMouseDown);
    return () => document.removeEventListener("mousedown", onDocMouseDown);
  }, [userMenuOpen]);

  useEffect(() => {
    setUserMenuOpen(false);
  }, [location.pathname]);

  return (
    <aside
      className={[
        "fixed left-0 top-0 z-50 h-screen",
        "bg-neutral-950 text-white",
        "border-r border-white/10",
        "flex flex-col",
        collapsed ? "w-20" : "w-64",
      ].join(" ")}
    >
      <div className="h-16 px-4 flex items-center border-b border-white/10">
        <div className="font-semibold tracking-wide select-none">
          {collapsed ? "RA" : "RoboArm"}
        </div>
      </div>

      <nav className="p-2 flex-1 overflow-y-auto">
        <ul className="space-y-1">
          {items.map((it) => {
            const active = isActive(it.key);
            return (
              <li key={it.key}>
                <button
                  type="button"
                  onClick={() => navigate(it.key)}
                  className={[
                    "relative w-full flex items-center gap-3",
                    "px-3 py-3 rounded-lg text-left",
                    "transition",
                    active
                      ? "bg-white/10 text-white"
                      : "text-white/70 hover:text-white hover:bg-white/5",
                    collapsed ? "justify-center" : "",
                  ].join(" ")}
                >
                  {active && (
                    <span className="absolute left-0 top-2 bottom-2 w-1 rounded-r bg-white/80" />
                  )}

                  <span className="text-lg">{it.icon}</span>
                  {!collapsed && <span className="text-sm">{it.label}</span>}
                </button>
              </li>
            );
          })}
        </ul>
      </nav>

      <div
        ref={userMenuRef}
        className={[
          "p-3 border-t border-white/10",
          "relative",
          collapsed ? "flex justify-center" : "",
        ].join(" ")}
      >
        {userMenuOpen && (
          <div
            className={[
              "absolute bottom-full left-3 right-3 mb-2",
              "rounded-lg border border-white/10",
              "bg-neutral-950",
              "p-3",
            ].join(" ")}
            role="menu"
            aria-label="User menu"
          >
            <div className="text-sm font-semibold truncate">{displayName}</div>
            <div className="text-xs text-white/70 truncate">{email || ""}</div>
          </div>
        )}

        <button
          type="button"
          onClick={() => setUserMenuOpen((v) => !v)}
          className={[
            "w-full flex items-center gap-3",
            "rounded-lg",
            "hover:bg-white/5",
            collapsed ? "justify-center" : "px-2 py-2",
          ].join(" ")}
          aria-haspopup="menu"
          aria-expanded={userMenuOpen}
        >
          <div
            className={[
              "h-9 w-9 rounded-full",
              "bg-white/10 border border-white/10",
              "flex items-center justify-center",
              "text-sm font-semibold",
              "select-none",
            ].join(" ")}
            title={displayName}
            aria-label={displayName}
          >
            {avatarText}
          </div>

          {!collapsed && (
            <div className="min-w-0 text-left">
              <div className="text-sm font-medium truncate">{displayName}</div>
            </div>
          )}
        </button>
      </div>
    </aside>
  );
}

export default SidebarAdmin;