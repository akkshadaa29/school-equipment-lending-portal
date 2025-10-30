import React, { useState, useEffect } from "react";
import "./EquipmentModal.css";

interface EquipmentModalProps {
  open: boolean;
  initial?: any;
  onClose: () => void;
  onSave: (data: any) => void;
}

const EquipmentModal: React.FC<EquipmentModalProps> = ({
  open,
  initial,
  onClose,
  onSave,
}) => {
  const [form, setForm] = useState({
    name: "",
    category: "",
    quantity: 1,
    available: true,
    condition: "Good", // NEW: condition field
  });

  useEffect(() => {
    if (initial) {
      const { availableUnits, condition, ...rest } = initial;
      setForm({
        ...rest,
        quantity: availableUnits ?? 1,
        condition: condition ?? "Good",
      });
    } else {
      setForm({
        name: "",
        category: "",
        quantity: 1,
        available: true,
        condition: "Good",
      });
    }
  }, [initial, open]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "number" ? Number(value) : value,
    }));
  };

  const handleSubmit = () => {
    if (!form.name.trim()) {
      alert("Name is required");
      return;
    }
    if (!form.category.trim()) {
      alert("Category is required");
      return;
    }
    onSave(form);
    onClose();
  };

  if (!open) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()} // allows typing inside modal
      >
        <h3>{initial ? "Edit Equipment" : "Add Equipment"}</h3>

        <div className="form-group">
          <label>Name</label>
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Equipment name"
          />
        </div>

        <div className="form-group">
          <label>Category</label>
          <input
            name="category"
            value={form.category}
            onChange={handleChange}
            placeholder="Category"
          />
        </div>

        <div className="form-group">
          <label>Quantity</label>
          <input
            type="number"
            name="quantity"
            value={form.quantity}
            min={1}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label>Condition</label>
          <select
            name="condition"
            value={(form as any).condition}
            onChange={handleChange}
          >
            <option value="New">New</option>
            <option value="Good">Good</option>
            <option value="Fair">Fair</option>
            <option value="Poor">Poor</option>
          </select>
        </div>

        <div className="modal-actions">
          <button onClick={onClose}>Cancel</button>
          <button onClick={handleSubmit} className="btn-primary">
            Save
          </button>
        </div>
      </div>
    </div>
  );
};

export default EquipmentModal;
