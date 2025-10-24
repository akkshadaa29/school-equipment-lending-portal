// src/pages/AdminEquipment.tsx
import React, { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchEquipments, createEquipment, updateEquipment, deleteEquipment } from "../service/equipment";
import EquipmentModal from "../components/EquipmentModal";
import { useAuth } from "../context/AuthContext"; // adjust path if different

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

  // create
  const createMut = useMutation({
    mutationFn: (payload: any) => createEquipment(payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment created");
    },
    onError: (err: any) => alert(err?.message ?? "Create failed"),
  });

  // update
  const updateMut = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: any }) => updateEquipment(id, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment updated");
    },
    onError: (err: any) => alert(err?.message ?? "Update failed"),
  });

  // delete
  const deleteMut = useMutation({
    mutationFn: (id: number | string) => deleteEquipment(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Equipment deleted");
    },
    onError: (err: any) => alert(err?.message ?? "Delete failed"),
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

    if (payload.id) {
      await updateMut.mutateAsync({ id: payload.id, payload });
    } else {
      await createMut.mutateAsync(payload);
    }
  };

  const handleDelete = (id: number | string) => {
    if (!confirm("Delete this equipment?")) return;
    deleteMut.mutate(id);
  };

  // basic columns for table
  const columns = useMemo(() => ["Name", "Category", "Condition", "Available Units", "Actions"], []);

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
        <h2 style={{ margin: 0 }}>Manage Equipment</h2>
        <div>
          <button className="btn-primary" onClick={openAdd}>+ Add equipment</button>
        </div>
      </div>

      <div className="card table-card" style={{ padding: 0 }}>
        <table className="equip-table" style={{ width: "100%" }}>
          <thead>
            <tr>
              {columns.map((c) => <th key={c} style={{ padding: "16px 20px", textAlign: "left" }}>{c}</th>)}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr><td colSpan={5} style={{ padding: 20 }}>Loading…</td></tr>
            )}

            {!isLoading && rows.length === 0 && (
              <tr><td colSpan={5} style={{ padding: 20 }}>No equipment found.</td></tr>
            )}

            {!isLoading && rows.map((r: any) => (
              <tr key={r.id}>
                <td style={{ padding: "16px 20px" }}>{r.name}</td>
                <td style={{ padding: "16px 20px" }}>{r.category}</td>
                <td style={{ padding: "16px 20px" }}>{r.condition ?? "Unknown"}</td>
                <td style={{ padding: "16px 20px" }}>{r.availableUnits ?? r.quantity ?? 0}</td>
                <td style={{ padding: "16px 20px" }}>
                  <button className="btn-ghost" onClick={() => openEdit(r)} style={{ marginRight: 8 }}>Edit</button>
                  <button className="btn-small" onClick={() => handleDelete(r.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <EquipmentModal
        open={modalOpen}
        initial={editing ?? undefined}
        onClose={() => setModalOpen(false)}
        onSave={handleSave}
      />
    </div>
  );
};

export default AdminEquipment;