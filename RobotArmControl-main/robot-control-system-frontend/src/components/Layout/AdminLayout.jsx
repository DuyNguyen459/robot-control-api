import React, { useMemo, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { MenuUnfoldOutlined, MenuFoldOutlined } from "@ant-design/icons";
import SidebarAdmin from "./SidebarAdmin";
import HeaderAdmin from "./HeaderAdmin";
import { authApi } from "../../api/authApi";

function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  const user = useMemo(() => {
    const name = localStorage.getItem("username") || "Admin";
    return { name, avatarUrl: "" };
  }, []);

  //hardcode nhà máy 
    const factories = [
      {id: 1, name:"Nhà máy Bảo Ân", location: "Huế", status:"Hoạt động"},
      {id: 2, name:"Nhà máy Anh Khoa", location: "Hồ Chí Minh", status:"Hoạt động"},
      {id: 3, name:"Nhà máy Đình Duy", location: "Gia lai", status:"Không hoạt động"},
      {id: 4, name:"Nhà máy Trọng Nhã", location: "Hà Nội", status:"Không hoạt động"},
      {id: 5, name:"Nhà máy Trần Quang", location: "Đà Nẵng", status:"Hoạt động"},
    ]

  return (
    <div className="min-h-screen w-screen bg-neutral-950 text-white">
      <SidebarAdmin collapsed={collapsed} />

      <div className={collapsed ? "ml-20" : "ml-64"}>
        <header className="sticky top-0 z-40 h-16 px-4 flex items-center gap-4 bg-neutral-950/80 backdrop-blur border-b border-white/10">
          <button
            type="button"
            onClick={() => setCollapsed((v) => !v)}
            className="h-10 w-10 grid place-items-center rounded-lg bg-white/5 hover:bg-white/10 text-white/80 hover:text-white transition"
            aria-label="Toggle sidebar"
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </button>

          <div className="flex-1 min-w-0">
            <HeaderAdmin
              factories={factories}
              user={user}
              onSelectFactory={() => {}}
              onProfile={() => {}}
              onSetting={() => navigate("/admin/settings")}
              onLogout={() => {
                authApi.logout();
                navigate("/", { replace: true });
              }}
            />
          </div>
        </header>

        <main className="p-6">
          <div>
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}

export default AdminLayout;
