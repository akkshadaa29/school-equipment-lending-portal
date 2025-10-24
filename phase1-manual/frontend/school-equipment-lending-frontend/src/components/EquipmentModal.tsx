// src/components/EquipmentModal.tsx
import React, { useEffect, useState } from "react";

type EquipmentPayload = {
  id?: number;
  name?: string;
  category?: string;
  available?: boolean;
  quantity?: number;
};

type Props = {
  open: boolean;
  onClose: () => void;
  onSave?: (data: EquipmentPayload) => Promise<void> | void;
  initial?: Partial<EquipmentPayload>;
};

const EquipmentModal: React.FC<Props> = ({
  open = false,
  onClose = () => {},
  onSave,
  initial = {},
}) => {
  // ALWAYS declare hooks in the same order, before any early returns
  const [name, setName] = useState(initial.name ?? "");
  const [category, setCategory] = useState(initial.category ?? "");
  const [available, setAvailable] = useState<boolean>(initial.available ?? true);
  const [quantity, setQuantity] = useState<number>(initial.quantity ?? 1);
  const [saving, setSaving] = useState(false);

  // Reset fields when `open` or `initial` changes
  useEffect(() => {
    if (open) {
      setName(initial.name ?? "");
      setCategory(initial.category ?? "");
      setAvailable(initial.available ?? true);
      setQuantity(initial.quantity ?? 1);
    }
  }, [open, initial]);

  // ESC handler (always registered/unregistered; uses onClose from props)
  useEffect(() => {
    const onKey = (ev: KeyboardEvent) => {
      if (ev.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  // Fast bailout for rendering (hooks already declared)
  if (!open) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      alert("Name is required");
      return;
    }
    setSaving(true);
    try {
      if (onSave) {
        await onSave({
          id: initial.id,
          name: name.trim(),
          category: category.trim(),
          available,
          quantity,
        });
      }
      onClose();
    } catch (err: any) {
      console.error("Modal save error:", err);
      alert(err?.message ?? "Failed to save");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="modal-backdrop"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label={initial.id ? "Edit equipment" : "Add equipment"}
    >
      <form
        className="modal-card"
        onClick={(e) => e.stopPropagation()}
        onSubmit={handleSubmit}
      >
        <div
          className="modal-header"
          style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}
        >
          <h3 style={{ margin: 0 }}>{initial.id ? "Edit equipment" : "Add equipment"}</h3>
          <button type="button" className="btn-ghost" onClick={onClose} aria-label="Close">
            Close
          </button>
        </div>

        <div style={{ display: "grid", gap: 12, marginTop: 8 }}>
          <div>
            <label style={{ fontWeight: 600 }}>Name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="auth-input"
              autoFocus
            />
          </div>

          <div>
            <label style={{ fontWeight: 600 }}>Category</label>
            <input
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="auth-input"
            />
          </div>

          <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
            <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <input type="checkbox" checked={available} onChange={(e) => setAvailable(e.target.checked)} />
              <span style={{ fontWeight: 600 }}>Available</span>
            </label>

            <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
              <span style={{ fontWeight: 600 }}>Quantity</span>
              <input
                type="number"
                min={0}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                style={{ width: 96 }}
              />
            </label>
          </div>
        </div>

        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
          <button type="button" className="btn-ghost" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? "Saving..." : initial.id ? "Save" : "Create"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EquipmentModal;
