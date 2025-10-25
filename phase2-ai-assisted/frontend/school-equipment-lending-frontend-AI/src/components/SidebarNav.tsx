// src/components/SidebarNav.tsx
import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const SidebarNav: React.FC = () => {
  const { hasRole } = useAuth();

  return (
    <nav className="nav">
      <ul className="nav-list">
        <li><Link to="/">Dashboard</Link></li>
        <li><Link to="/bookings">My Bookings</Link></li>
        <li><Link to="/request">Request equipment</Link></li>
        <li><Link to="/loans">My Loans</Link></li>
        {hasRole("ROLE_ADMIN") && (
          <li><Link to="/admin/bookings">Admin: Pending</Link></li>
        )}
      </ul>
    </nav>
  );
};

export default SidebarNav;
