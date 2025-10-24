// src/pages/AdminBookings.tsx
import React from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getPendingBookings,
  approveBooking,
  rejectBooking,
} from "../service/booking";
import { BookingList } from "../components/BookingList";

const AdminBookings: React.FC = () => {
  const qc = useQueryClient();

  // ✅ Updated to React Query v5 syntax
  const {
    data: pending = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["bookings", "pending"],
    queryFn: getPendingBookings,
    staleTime: 60_000,
  });

  // ✅ Updated mutation syntax
  const approve = useMutation({
    mutationFn: (id: number) => approveBooking(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bookings", "pending"] });
      qc.invalidateQueries({ queryKey: ["equipments"] });
      qc.invalidateQueries({ queryKey: ["bookings", "me"] });
    },
  });

  const reject = useMutation({
    mutationFn: (id: number) => rejectBooking(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bookings", "pending"] });
    },
  });

  const handleApprove = async (id: number) => {
    try {
      await approve.mutateAsync(id);
      alert("Booking approved successfully!");
    } catch (err: any) {
      console.error(err);
      alert(err?.response?.data?.message ?? "Approve failed");
    }
  };

  const handleReject = async (id: number) => {
    try {
      await reject.mutateAsync(id);
      alert("Booking rejected!");
    } catch (err: any) {
      console.error(err);
      alert(err?.response?.data?.message ?? "Reject failed");
    }
  };

  return (
    <div>
      <h2>Pending Bookings (Admin)</h2>

      {isLoading && (
        <div className="card table-card">Loading pending bookings…</div>
      )}

      {isError && (
        <div className="card table-card">
          Error fetching bookings. Please refresh.
        </div>
      )}

      {!isLoading && !isError && (
        <BookingList
          bookings={pending}
          isAdmin
          onApprove={handleApprove}
          onReject={handleReject}
        />
      )}
    </div>
  );
};

export default AdminBookings;