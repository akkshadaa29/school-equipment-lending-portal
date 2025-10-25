// src/components/Sidebar.tsx
import React from "react";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

const Sidebar: React.FC = () => {
  const { hasRole, logout, user } = useAuth();

  return (
    <aside className="sidebar">
      <div className="brand">EquipHub</div>
      <nav className="nav">
        <ul className="nav-list">
          <li><Link to="/">Dashboard</Link></li>
          <li><Link to="/bookings">My Bookings</Link></li>
          <li><Link to="/request">Request equipment</Link></li>
          <li><Link to="/loans">My Loans</Link></li>
          {hasRole("ROLE_ADMIN") && <li><Link to="/admin/bookings">Admin: Pending</Link></li>}
          {hasRole("ROLE_ADMIN") && (<li><Link to="/admin/equipment">Admin: Equipment</Link></li>)}
        </ul>
      </nav>
      <div className="sidebar-footer">
        <div className="user-name">{user?.username}</div>
        <div className="logout" onClick={logout}>
          Logout
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
