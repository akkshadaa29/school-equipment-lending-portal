// src/pages/MyBookingsPage.tsx
import React from "react";
import { useQuery } from "@tanstack/react-query";
import {BookingList} from "../components/BookingList";
import type { Booking } from "../service/booking";

/**
 * Fetch bookings for current user.
 * Adjust the endpoint to your API/service if you have a dedicated service helper.
 */
const fetchMyBookings = async (): Promise<Booking[]> => {
  const res = await fetch("/api/bookings/my");
  if (!res.ok) throw new Error("Failed to fetch bookings");
  return res.json();
};

const MyBookingsPage: React.FC = () => {
  const { data: bookings = [], isLoading, error, refetch } = useQuery<Booking[], Error>({
    queryKey: ["bookings", "mine"],
    queryFn: fetchMyBookings,
  });

  return (
    <div>
      <h2 className="section-title">My Bookings</h2>

      {isLoading && <div className="card">Loading bookings...</div>}
      {error && <div className="card">Failed to load bookings: {error.message}</div>}

      {!isLoading && !error && (
        <div className="card table-card">
          <BookingList bookings={bookings} onApprove={undefined} onReject={undefined} />
        </div>
      )}
    </div>
  );
};

export default MyBookingsPage;
