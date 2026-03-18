import React, { useMemo } from "react";
import { useLocation } from "react-router-dom";
import { Dropdown } from "antd";
import { LogoutOutlined, SettingOutlined, UserOutlined } from "@ant-design/icons";
import { authApi } from "../../api/authApi";
import { getRole, isAdminRole, isOperatorRole } from "../../utils/auth";

export default function HeaderAdmin({
    user = { name: "Admin", avatarUrl: "" },
    onSetting,
    onLogout,
}) {
    const location = useLocation();

    const role = getRole();
    const isAdmin = isAdminRole(role);
    const isOperator = isOperatorRole(role);

    const crumb = useMemo(() => {
        const path = location.pathname;
        if (path.startsWith("/admin/factories")) return "Factories";
        if (path.startsWith("/admin/areas")) return "Areas";
        if (path.startsWith("/admin/hubs")) return "Hubs";
        if (path.startsWith("/admin/devices")) return "Devices";
        if (path.startsWith("/admin/users")) return "Users";
        if (path.startsWith("/admin/ai-camera")) return "AI Camera";
        if (path.startsWith("/admin/settings")) return "Settings";
        if (path.startsWith("/admin/dashboard")) return isOperator ? "Logs" : "Dashboard";
        return "Management";
    }, [isOperator, location.pathname]);

    const menuItems = useMemo(() => {
        const base = [
            {
                key: "logout",
                label: "Logout",
                icon: <LogoutOutlined />,
            },
        ];

        if (!isAdmin) return base;

        return [
            {
                key: "settings",
                label: "Cài đặt",
                icon: <SettingOutlined />,
            },
            {
                type: "divider",
            },
            ...base,
        ];
    }, [isAdmin]);

    const menu = useMemo(() => {
        return {
            items: menuItems,
            onClick: ({ key }) => {
                if (key === "settings") {
                    onSetting?.();
                    return;
                }
                if (key === "logout") {
                    try {
                        authApi.logout();
                    } finally {
                        onLogout?.();
                        window.location.href = "/?login=1";
                    }
                }
            },
        };
    }, [menuItems, onLogout, onSetting]);

    return (
        <div className="w-full flex items-center justify-between gap-4 min-w-0">
            <div className="min-w-0">
                <div className="text-sm font-semibold text-white">
                    {isOperator ? "Robot Arm Operator" : "Robot Arm Admin"}
                </div>
                <div className="text-xs text-white/60 truncate">
                    Management <span className="mx-2 text-white/30">/</span> {crumb}
                </div>
            </div>

            <div className="flex items-center gap-2">
                {isAdmin && (
                    <button
                        type="button"
                        onClick={onSetting}
                        className="h-9 w-9 rounded-full grid place-items-center bg-white/5 hover:bg-white/10 text-white/80 hover:text-white transition"
                        aria-label="Settings"
                    >
                        <SettingOutlined />
                    </button>
                )}

                <Dropdown trigger={["click"]} menu={menu} placement="bottomRight">
                    <button
                        type="button"
                        className="h-9 w-9 rounded-full grid place-items-center bg-white/5 hover:bg-white/10 text-white/80 hover:text-white transition overflow-hidden"
                        aria-label="User"
                        title={user?.name || "User"}
                    >
                        {user?.avatarUrl ? (
                            <img
                                src={user.avatarUrl}
                                alt={user?.name ? `${user.name} avatar` : "User avatar"}
                                className="h-full w-full object-cover"
                            />
                        ) : (
                            <UserOutlined />
                        )}
                    </button>
                </Dropdown>
            </div>
        </div>
    );
}

