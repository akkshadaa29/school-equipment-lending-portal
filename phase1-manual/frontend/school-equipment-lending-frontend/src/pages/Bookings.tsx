// src/pages/Bookings.tsx
import React from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getMyBookings, getMyLoans, returnLoan } from "../service/booking";
import { BookingList } from "../components/BookingList";

const BookingsPage: React.FC = () => {
  const qc = useQueryClient();

  // My bookings
  const { data: myBookings = [], isLoading: bookingsLoading, isError: bookingsError, refetch: refetchBookings } = useQuery({
    queryKey: ["bookings", "me"],
    queryFn: getMyBookings,
    staleTime: 60_000,
  });

  // My loans
  const { data: myLoans = [], isLoading: loansLoading } = useQuery({
    queryKey: ["loans", "me"],
    queryFn: getMyLoans,
    staleTime: 60_000,
  });

  // return loan mutation
  const returnMut = useMutation({
    mutationFn: (loanId: number) => returnLoan(loanId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["loans", "me"] });
      qc.invalidateQueries({ queryKey: ["equipments"] });
      alert("Loan returned");
    },
  });

  return (
    <div>
      <h2 className="section-title">My Bookings</h2>

      {bookingsLoading && <div className="card table-card">Loading bookings…</div>}
      {bookingsError && <div className="card table-card">Failed to load bookings.</div>}

      {!bookingsLoading && !bookingsError && (
        <div className="card table-card">
          <BookingList bookings={myBookings} />
        </div>
      )}

      <h2 style={{ marginTop: 28 }} className="section-title">My Loans</h2>

      {loansLoading && <div className="card table-card">Loading loans…</div>}

      {!loansLoading && myLoans.length === 0 && <div className="card table-card">No active loans</div>}

      {!loansLoading && myLoans.length > 0 && (
        <div className="card table-card">
          <table className="equip-table">
            <thead>
              <tr>
                <th>Equipment</th>
                <th>Qty</th>
                <th>Due</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {myLoans.map((l: any) => (
                <tr key={l.id}>
                  <td>{l.equipmentName ?? `#${l.equipmentId}`}</td>
                  <td>{l.quantity}</td>
                  <td>{l.dueAt ? new Date(l.dueAt).toLocaleString() : "—"}</td>
                  <td>{l.status}</td>
                  <td>
                    {l.status === "BORROWED" && (
                      <button
                        onClick={() => returnMut.mutate(Number(l.id))}
                        className="btn-small"
                        disabled={returnMut.isPending}
                      >
                        {returnMut.isPending ? "Returning..." : "Return"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default BookingsPage;
