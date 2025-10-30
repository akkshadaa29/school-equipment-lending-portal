// src/components/Sidebar.tsx
import React from "react";
import { useAuth } from "../context/AuthContext";
import { NavLink } from "react-router-dom";

const Sidebar: React.FC = () => {
  const { hasRole, logout, user } = useAuth();

  const linkClass =
    (base: string) =>
    ({ isActive }: { isActive: boolean }) =>
      `nav-item ${base}${isActive ? " active" : ""}`;

  return (
    <aside className="sidebar" role="navigation" aria-label="Main sidebar">
      <div className="brand">EquipHub</div>

      <nav className="nav" aria-label="Primary">
        <ul className="nav-list" style={{ listStyle: "none", padding: 0, margin: 0 }}>
          <li>
            <NavLink to="/" className={linkClass("dashboard")} end>
              Dashboard
            </NavLink>
          </li>

          <li>
            <NavLink to="/bookings" className={linkClass("bookings")}>
              My Bookings
            </NavLink>
          </li>

          <li>
            <NavLink to="/request" className={linkClass("request")}>
              Request equipment
            </NavLink>
          </li>

          <li>
            <NavLink to="/loans" className={linkClass("loans")}>
              My Loans
            </NavLink>
          </li>

          {hasRole("ROLE_ADMIN") && (
            <li>
              <NavLink to="/admin/bookings" className={linkClass("pending")}>
                {/* small inline icon + label for clarity */}
                <span className="nav-admin-label">Admin: Pending</span>
              </NavLink>
            </li>
          )}

          {hasRole("ROLE_ADMIN") && (
            <li>
              <NavLink to="/admin/equipment" className={linkClass("equipment")}>
                <span className="nav-admin-label">Admin: Equipment</span>
              </NavLink>
            </li>
          )}
        </ul>
      </nav>

      <div className="sidebar-footer">
        <div className="user-name" title={user?.username}>
          <span className="footer-icon" aria-hidden>👤</span>
          <span className="footer-label">{user?.username || "user"}</span>
        </div>

        <div
          className="logout"
          onClick={logout}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") logout();
          }}
          aria-label="Logout"
        >
          <span className="footer-icon" aria-hidden>🚪</span>
          <span className="footer-label">Logout</span>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
