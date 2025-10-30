import React, { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { getMyBookings } from "../service/booking";
import { BookingList } from "../components/BookingList";
import { useAutoRefresh } from "../context/AutoRefreshContext";

const MyBookingsPage: React.FC = () => {
  const { lastRefresh } = useAutoRefresh();

  const {
    data: myBookings = [],
    isLoading: bookingsLoading,
    isError: bookingsError,
    refetch: refetchBookings,
  } = useQuery({
    queryKey: ["bookings", "me"],
    queryFn: getMyBookings,
    staleTime: 60_000,
  });

  useEffect(() => {
    refetchBookings();
  }, [lastRefresh, refetchBookings]);

  return (
    <div>
      <h2 className="section-title">My Bookings</h2>

      {bookingsLoading && <div className="card table-card">Loading bookings…</div>}
      {bookingsError && <div className="card table-card">Failed to load bookings.</div>}

      {!bookingsLoading && !bookingsError && (
        // kept the card wrapper but BookingList will provide the scroll area
        <div className="card table-card">
          <BookingList bookings={myBookings} />
        </div>
      )}
    </div>
  );
};

export default MyBookingsPage;
