// src/pages/AdminEquipment.tsx
import React, { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchEquipments, createEquipment, updateEquipment, deleteEquipment } from "../service/equipment";
import EquipmentModal from "../components/EquipmentModal";
import { useAuth } from "../context/AuthContext"; // adjust path if different
import { Pencil, Trash2 } from "lucide-react";
import type { AxiosError } from "axios";

const AdminEquipment: React.FC = () => {
  const qc = useQueryClient();
  const { hasRole } = useAuth?.() ?? { hasRole: () => false };

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<any | null>(null);

  const { data: rows = [], isLoading } = useQuery({
    queryKey: ["equipments", { admin: true }],
    queryFn: () => fetchEquipments({}),
    staleTime: 60_000,
  });

  // helper to extract backend message from Axios error
  const extractErrorMessage = (err: any) => {
    const axiosErr = err as AxiosError<any>;
    if (axiosErr?.response?.data?.message) return axiosErr.response.data.message;
    if (axiosErr?.response?.data?.error) return axiosErr.response.data.error;
    return axiosErr?.message || "An unexpected error occurred";
  };

  // create
  const createMut = useMutation({
    mutationFn: (payload: any) => createEquipment(payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment created");
    },
    onError: (err: any) => {
      alert(extractErrorMessage(err));
    },
  });

  // update
  const updateMut = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: any }) => updateEquipment(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment updated");
    },
    onError: (err: any) => {
      alert(extractErrorMessage(err));
    },
  });

  // delete
  const deleteMut = useMutation({
    mutationFn: (id: number | string) => deleteEquipment(Number(id)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment deleted");
    },
    onError: (err: any) => {
      alert(extractErrorMessage(err));
    },
  });

  const openAdd = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const openEdit = (row: any) => {
    setEditing(row);
    setModalOpen(true);
  };

  const handleSave = async (payload: any) => {
    if (!hasRole || !hasRole("ROLE_ADMIN")) {
      alert("Permission denied");
      return;
    }

    try {
      if (payload.id) {
        await updateMut.mutateAsync({ id: payload.id, payload });
      } else {
        await createMut.mutateAsync(payload);
      }
      setModalOpen(false);
    } catch (e) {
      // error handled by onError already (mutations show alerts). no-op
    }
  };

  const handleDelete = async (id: number | string) => {
    if (!confirm("Delete this equipment?")) return;
    try {
      await deleteMut.mutateAsync(Number(id));
    } catch (e) {
      // onError already shows message; swallow
    }
  };

  const columns = useMemo(() => ["Name", "Category", "Condition", "Available Units", "Actions"], []);

  // styles unchanged...
  const styles: Record<string, React.CSSProperties> = {
    headerRowWrap: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 },
    card: { padding: 0 },
    tableWrapper: {
      maxHeight: "72vh",
      overflowY: "auto",
      borderTop: "1px solid #e8edf1",
      borderBottom: "1px solid #e8edf1",
      borderRadius: 8,
      boxShadow: "0 6px 18px rgba(22, 40, 60, 0.04)",
      background: "#fff",
    },
    table: {
      width: "100%",
      borderCollapse: "separate",
      borderSpacing: 0,
      tableLayout: "auto",
      minWidth: "720px",
    },
    th: {
      padding: "14px 18px",
      textAlign: "left",
      position: "sticky",
      top: 0,
      background: "linear-gradient(180deg,#ffffff 0%, #fbfdff 60%)",
      zIndex: 4,
      borderBottom: "1px solid #e6eef3",
      fontWeight: 700,
      textTransform: "uppercase",
      fontSize: 12,
      letterSpacing: "0.06em",
      color: "#16324a",
    },
    thFirstCol: {
      background: "linear-gradient(90deg,#f7fbff,#ffffff)",
      borderRight: "1px solid #eef6fb",
      boxShadow: "2px 0 0 rgba(0,0,0,0.02) inset",
    },
    td: { padding: "14px 18px", borderBottom: "1px solid #f5f8fa", verticalAlign: "middle", fontSize: 14, color: "#23313f" },
    tdFirstCol: {
      position: "sticky",
      left: 0,
      background: "#fff",
      borderRight: "1px solid #f1f6fb",
      zIndex: 3,
    },
    rowHover: { background: "rgba(34,50,66,0.02)" },
    zebra: { background: "#fbfdff" },
    actionsWrap: { display: "flex", gap: 8, alignItems: "center" },
    btnGhost: { background: "transparent", border: "1px solid transparent", padding: "6px 10px", cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 8, borderRadius: 6 },
    btnDestructive: { background: "rgba(220,38,38,0.06)", color: "#b91c1c", padding: "6px 10px", cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 8, borderRadius: 6, border: "1px solid rgba(220,38,38,0.12)" },
    btnSmallText: { fontSize: 13, lineHeight: 1, padding: 0, border: "none", background: "transparent", cursor: "pointer" },
    loadingRow: { padding: 20, textAlign: "center" },
  };

  return (
    <div>
      <div style={styles.headerRowWrap}>
        <h2 style={{ margin: 0 }}>Manage Equipment</h2>
        <div>
          <button className="btn-primary" onClick={openAdd}>
            + Add equipment
          </button>
        </div>
      </div>

      <div className="card table-card" style={styles.card}>
        <div style={styles.tableWrapper}>
          <table className="equip-table" style={styles.table}>
            <thead>
              <tr>
                {columns.map((c, idx) => (
                  <th key={c} style={{ ...styles.th, ...(idx === 0 ? styles.thFirstCol : {}) }}>
                    {c}
                  </th>
                ))}
              </tr>
            </thead>

            <tbody>
              {isLoading && (
                <tr>
                  <td colSpan={5} style={{ ...styles.td, ...styles.loadingRow }}>
                    Loading…
                  </td>
                </tr>
              )}

              {!isLoading && rows.length === 0 && (
                <tr>
                  <td colSpan={5} style={{ ...styles.td, ...styles.loadingRow }}>
                    No equipment found.
                  </td>
                </tr>
              )}

              {!isLoading &&
                rows.map((r: any, rowIndex: number) => {
                  const zebra = rowIndex % 2 === 1;
                  return (
                    <tr
                      key={r.id}
                      style={zebra ? styles.zebra : undefined}
                      onMouseEnter={(e) => (e.currentTarget.style.background = String(styles.rowHover.background))}
                      onMouseLeave={(e) => (e.currentTarget.style.background = zebra ? String(styles.zebra.background) : "transparent")}
                    >
                      <td style={{ ...styles.td, ...styles.tdFirstCol }}>{r.name}</td>
                      <td style={styles.td}>{r.category}</td>
                      <td style={styles.td}>{r.condition ?? "Unknown"}</td>
                      <td style={styles.td}>{r.availableUnits ?? r.quantity ?? 0}</td>
                      <td style={styles.td}>
                        <div style={styles.actionsWrap}>
                          <button aria-label={`Edit ${r.name}`} className="btn-ghost" onClick={() => openEdit(r)} style={styles.btnGhost}>
                            <Pencil size={14} strokeWidth={2} style={{ color: "#0f766e" }} />
                            <span style={{ fontSize: 13, color: "#0f3b36" }}>Edit</span>
                          </button>

                          <button aria-label={`Delete ${r.name}`} className="btn-destructive" onClick={() => handleDelete(r.id)} style={styles.btnDestructive}>
                            <Trash2 size={14} strokeWidth={2} style={{ color: "#b91c1c" }} />
                            <span style={{ fontSize: 13, color: "#7f1d1d" }}>Delete</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
            </tbody>
          </table>
        </div>
      </div>

      <EquipmentModal open={modalOpen} initial={editing ?? undefined} onClose={() => setModalOpen(false)} onSave={handleSave} />
    </div>
  );
};

export default AdminEquipment;
