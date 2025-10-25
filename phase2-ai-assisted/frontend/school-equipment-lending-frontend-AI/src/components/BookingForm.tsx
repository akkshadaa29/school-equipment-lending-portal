// src/components/BookingForm.tsx
import React, { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createBooking } from "../service/booking";
import type { Equipment } from "../service/equipment";
import { toast } from "react-hot-toast";

type Props = {
  equipments: Equipment[];
};

const isoLocal = (d: Date) => d.toISOString().slice(0, 16);

export const BookingForm: React.FC<Props> = ({ equipments }) => {
  const [equipmentId, setEquipmentId] = useState<number | "">("");
  const [quantity, setQuantity] = useState<number>(1);
  const [startAt, setStartAt] = useState<string>(isoLocal(new Date()));
  const [endAt, setEndAt] = useState<string>(
    isoLocal(new Date(Date.now() + 1000 * 60 * 60 * 24))
  );

  const qc = useQueryClient();

  const mutation = useMutation({
    mutationFn: createBooking,
    retry: false,
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["equipments"] });
      await qc.invalidateQueries({ queryKey: ["bookings", "me"] });
      toast.success("Booking request submitted successfully!", { id: "booking-success" });
    },
    onError: (err: any) => {
      const msg =
        err?.response?.data?.message?.replace(/^400 BAD_REQUEST\s*/, "") ||
        err?.response?.data?.error ||
        err?.message ||
        "Failed to create booking";
      toast.error(msg, { id: "booking-error" });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = typeof equipmentId === "number" ? equipmentId : Number(equipmentId);
    if (!id || quantity <= 0 || new Date(endAt) <= new Date(startAt)) {
      toast.error("Please select valid equipment, quantity, and dates", { id: "booking-error" });
      return;
    }

    mutation.mutate({
      equipmentId: id,
      quantityRequested: quantity,
      startAt: new Date(startAt).toISOString(),
      endAt: new Date(endAt).toISOString(),
    });
  };

  const minDateTime = isoLocal(new Date()); // <-- prevents past selection

  return (
    <form onSubmit={handleSubmit} className="request-form" noValidate>
      {/* Equipment + Quantity */}
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

      {/* Dates */}
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="from">From</label>
          <input
            id="from"
            type="datetime-local"
            value={startAt}
            onChange={(e) => setStartAt(e.target.value)}
            min={minDateTime} // <-- prevents past selection
          />
        </div>
        <div className="form-group">
          <label htmlFor="to">To</label>
          <input
            id="to"
            type="datetime-local"
            value={endAt}
            onChange={(e) => setEndAt(e.target.value)}
            min={minDateTime} // <-- prevents past selection
          />
        </div>
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-primary" disabled={mutation.isPending}>
          {mutation.isPending ? "Requesting..." : "Request Booking"}
        </button>
      </div>
    </form>
  );
};
