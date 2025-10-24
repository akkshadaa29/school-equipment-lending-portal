// src/components/BookingForm.tsx
import React, { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createBooking } from "../service/booking";
import type { Equipment } from "../service/equipment";

type Props = {
  equipments: Equipment[];
  onSuccess?: () => void;
};

const isoLocal = (d: Date) => d.toISOString().slice(0, 16);

export const BookingForm: React.FC<Props> = ({ equipments, onSuccess }) => {
  const [equipmentId, setEquipmentId] = useState<number | "">("");
  const [quantity, setQuantity] = useState<number>(1);
  const [startAt, setStartAt] = useState<string>(isoLocal(new Date()));
  const [endAt, setEndAt] = useState<string>(
    isoLocal(new Date(Date.now() + 1000 * 60 * 60 * 24))
  );

  const qc = useQueryClient();

  const { mutateAsync, isPending } = useMutation({
    mutationFn: createBooking,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["equipments"] });
      qc.invalidateQueries({ queryKey: ["bookings"] });
      onSuccess?.();
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const id = typeof equipmentId === "number" ? equipmentId : Number(equipmentId);
    if (!id || quantity <= 0 || new Date(endAt) <= new Date(startAt)) {
      alert("Please select an equipment, quantity > 0 and end > start");
      return;
    }

    try {
      await mutateAsync({
        equipmentId: id,
        quantity,
        startAt: new Date(startAt).toISOString(),
        endAt: new Date(endAt).toISOString(),
      });
      alert("Booking requested");
    } catch (err: any) {
      console.error(err);
      alert(err?.response?.data?.message ?? "Booking failed");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="request-form">
      {/* Row 1: Equipment + Quantity */}
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="equipment">Equipment</label>
          <select
            id="equipment"
            value={equipmentId}
            onChange={(e) =>
              setEquipmentId(e.target.value === "" ? "" : Number(e.target.value))
            }
          >
            <option value="">Select Equipment</option>
            {equipments.map((eq) => (
              <option key={eq.id} value={eq.id}>
                {eq.name} ({eq.availableUnits ?? eq.quantity ?? "?"} available)
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="quantity">Quantity</label>
          <input
            id="quantity"
            type="number"
            min={1}
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
          />
        </div>
      </div>

      {/* Row 2: From + To */}
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="from">From</label>
          <input
            id="from"
            type="datetime-local"
            value={startAt}
            onChange={(e) => setStartAt(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="to">To</label>
          <input
            id="to"
            type="datetime-local"
            value={endAt}
            onChange={(e) => setEndAt(e.target.value)}
          />
        </div>
      </div>

      {/* Submit button */}
      <div className="form-actions">
        <button type="submit" className="btn-primary" disabled={isPending}>
          {isPending ? "Requesting..." : "Request Booking"}
        </button>
      </div>
    </form>
  );
};
