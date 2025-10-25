// src/components/SidebarShortcuts.tsx
import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const SidebarShortcuts: React.FC = () => {
  const navigate = useNavigate();
  const { hasRole } = useAuth();

  return (
    <div className="shortcut-card">
      <h4 className="shortcut-title">Quick actions</h4>

      <button
        className="shortcut-btn"
        onClick={() => {
          navigate("/request");
          // optional: smooth scroll or focus logic can be added in Request page
        }}
        aria-label="Request equipment"
        title="Request equipment"
      >
        Request equipment
      </button>

      <button
        className="shortcut-btn"
        style={{ marginTop: 8, background: "transparent", border: "1px solid rgba(255,255,255,0.08)", color: "#fff" }}
        onClick={() => navigate("/bookings")}
        aria-label="View my bookings"
        title="View my bookings"
      >
        My bookings
      </button>

      {hasRole("ROLE_ADMIN") && (
        <div style={{ marginTop: 10 }}>
          <button
            className="shortcut-btn"
            style={{ background: "#f6d365", color: "#1f2937" }}
            onClick={() => navigate("/admin/bookings")}
            aria-label="Admin pending bookings"
            title="Admin pending bookings"
          >
            Pending approvals
          </button>
        </div>
      )}
    </div>
  );
};

export default SidebarShortcuts;