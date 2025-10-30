// src/components/BookingForm.tsx
import React, { useEffect, useState } from "react";
import "./BookingForm.css";
import { createBooking } from "../service/booking";
import type { AxiosError } from "axios";

interface Equipment {
  id: number;
  name: string;
  category?: string;
  availableUnits?: number;
  quantity?: number;
}

interface BookingFormProps {
  equipments: Equipment[];
  onSuccess: () => void;
  onError: (msg?: string) => void;
}

const todayIso = () => new Date().toISOString().split("T")[0];

const toStartDateTime = (dateIso: string) => `${dateIso}T00:00:00`;
const toEndDateTime = (dateIso: string) => `${dateIso}T23:59:59`;

export const BookingForm: React.FC<BookingFormProps> = ({ equipments, onSuccess, onError }) => {
  const [equipmentId, setEquipmentId] = useState<number | "">("");
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [quantity, setQuantity] = useState<number>(1);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (equipments?.length) {
      const firstAvailable = equipments.find((e) => (e.availableUnits ?? e.quantity ?? 0) > 0);
      const defaultId = firstAvailable ? firstAvailable.id : equipments[0].id;
      setEquipmentId((prev) => (prev === "" ? defaultId : prev));
      setQuantity(1);
    }
  }, [equipments]);

  const selectedEquipment = equipments.find((e) => e.id === equipmentId);
  const maxAvailable = selectedEquipment?.availableUnits ?? selectedEquipment?.quantity ?? 1;

  const handleStartDateChange = (v: string) => {
    const min = todayIso();
    const newStart = v >= min ? v : min;
    setStartDate(newStart);
    if (!endDate || endDate < newStart) {
      setEndDate(newStart);
    }
  };

  const handleEndDateChange = (v: string) => {
    const minEnd = startDate || todayIso();
    const newEnd = v >= minEnd ? v : minEnd;
    setEndDate(newEnd);
  };

  const extractErrMsg = (err: any) => {
    const axiosErr = err as AxiosError<any>;
    if (axiosErr?.response?.data?.message) return axiosErr.response.data.message;
    if (axiosErr?.response?.data?.error) return axiosErr.response.data.error;
    return axiosErr?.message || "Failed to submit booking";
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!equipmentId) {
      onError("Please select equipment.");
      return;
    }
    if (!startDate || !endDate) {
      onError("Please select start and end date.");
      return;
    }
    if (new Date(startDate) > new Date(endDate)) {
      onError("End date must be the same or after start date.");
      return;
    }
    if (!quantity || quantity < 1) {
      onError("Quantity must be at least 1.");
      return;
    }
    if (maxAvailable && quantity > maxAvailable) {
      onError(`Only ${maxAvailable} items available for this equipment.`);
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        equipmentId: Number(equipmentId),
        startAt: toStartDateTime(startDate),
        endAt: toEndDateTime(endDate),
        quantityRequested: quantity,
      };

      await createBooking(payload);

      onSuccess();

      setStartDate("");
      setEndDate("");
      setQuantity(1);
    } catch (err: any) {
      onError(extractErrMsg(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={submit} className="booking-form grid-2x2" aria-label="Request equipment form">
      <div className="field">
        <label htmlFor="equipment-select">Equipment</label>
        <select
          id="equipment-select"
          value={equipmentId}
          onChange={(e) => {
            const val = Number(e.target.value) || "";
            setEquipmentId(val);
            setQuantity(1);
          }}
        >
          <option value="">-- select --</option>
          {equipments.map((eq) => {
            const available = eq.availableUnits ?? eq.quantity ?? 0;
            return (
              <option
                key={eq.id}
                value={eq.id}
                disabled={available <= 0}
                title={available <= 0 ? "No units available" : `${available} available`}
              >
                {eq.name} {available !== undefined ? `(${available} available)` : ""}
              </option>
            );
          })}
        </select>
      </div>

      <div className="field">
        <label htmlFor="quantity">Quantity</label>
        <input
          id="quantity"
          type="number"
          min={1}
          max={maxAvailable ?? undefined}
          value={quantity}
          onChange={(e) => {
            const val = Number(e.target.value) || 1;
            const clamped = maxAvailable ? Math.min(val, maxAvailable) : val;
            setQuantity(clamped);
          }}
        />
      </div>

      <div className="field">
        <label htmlFor="start-date">Start date</label>
        <input id="start-date" type="date" value={startDate} min={todayIso()} onChange={(e) => handleStartDateChange(e.target.value)} />
      </div>

      <div className="field">
        <label htmlFor="end-date">End date</label>
        <input id="end-date" type="date" value={endDate} min={startDate || todayIso()} onChange={(e) => handleEndDateChange(e.target.value)} />
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-request" disabled={submitting}>
          {submitting ? "Requesting…" : "Request"}
        </button>
      </div>
    </form>
  );
};
