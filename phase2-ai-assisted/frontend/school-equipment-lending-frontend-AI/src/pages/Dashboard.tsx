// src/pages/Dashboard.tsx
import React, { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchEquipments } from "../service/equipment";
import { EquipmentTable } from "../components/EquipmentTable";
import { useAutoRefresh } from "../context/AutoRefreshContext";

const Dashboard: React.FC = () => {
  const [q, setQ] = useState("");
  const [category, setCategory] = useState("");
  const [availableOnly, setAvailableOnly] = useState(true);
  const { lastRefresh } = useAutoRefresh(); // <-- use the global refresh

  const { data: rows = [], isLoading, isError, refetch } = useQuery({
    queryKey: ["equipments", { q, category, availableOnly }],
    queryFn: () =>
      fetchEquipments({
        q: q || undefined,
        category: category || undefined,
        available: availableOnly ? true : undefined,
      }),
    keepPreviousData: true,
    staleTime: 60_000,
  });

  // Refetch equipment data whenever lastRefresh changes
  useEffect(() => {
    refetch();
  }, [lastRefresh, refetch]);

  return (
    <div className="dashboard">
      {/* Header and toolbar (unchanged) */}
      <header className="dashboard-header">
        <div className="header-top">
          <div className="brand-inline" aria-hidden>
            <div className="brand-mark">EH</div>
            <div className="brand-title">Equipment Inventory</div>
          </div>
          <div className="header-actions-desktop" aria-hidden />
        </div>

        <div className="header-toolbar">
          <div className="toolbar-left" aria-hidden />
          <div className="toolbar-controls">
            <div className="search control-search" style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden>
                <path fill="currentColor" d="M21 21l-4.35-4.35" />
              </svg>
              <input
                placeholder="Search equipment..."
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyDown={(e) => { if (e.key === "Enter") refetch(); }}
                aria-label="Search equipment"
                style={{ border: 0, outline: "none", width: 240 }}
              />
            </div>

            <select
              className="control-select"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              aria-label="Category"
            >
              <option value="">All categories</option>
              <option value="Photography">Photography</option>
              <option value="Sports">Sports</option>
              <option value="Electronics">Electronics</option>
              <option value="Lab">Lab</option>
              <option value="Fitness">Fitness</option>
            </select>

            <label className="control-available" style={{ marginLeft: 6 }}>
              <input
                type="checkbox"
                checked={availableOnly}
                onChange={(e) => setAvailableOnly(e.target.checked)}
              />
              <span>Available</span>
            </label>
          </div>
        </div>
      </header>

      {/* Content */}
      {isLoading && <div className="card table-card">Loading equipment…</div>}
      {isError && <div className="card table-card">Error loading equipment. Try refresh.</div>}
      {!isLoading && !isError && <EquipmentTable rows={rows} />}
    </div>
  );
};

export default Dashboard;
