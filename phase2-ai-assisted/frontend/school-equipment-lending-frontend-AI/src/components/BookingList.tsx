// src/components/BookingList.tsx
import React from "react";
import type { Booking } from "../service/booking";

type Props = {
  bookings: Booking[];
  isAdmin?: boolean;
  onApprove?: (id: number) => void;
  onReject?: (id: number) => void;
};

export const BookingList: React.FC<Props> = ({
  bookings,
  isAdmin = false,
  onApprove,
  onReject,
}) => {
  if (!bookings?.length)
    return <div className="card table-card">No bookings.</div>;

  return (
    <div className="card table-card">
      <table className="equip-table">
        <thead>
          <tr>
            <th>Equipment</th>
            <th>Quantity</th>
            <th>Range</th>
            <th>Status</th>
            <th>Requested by</th>
            {isAdmin && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {bookings.map((b) => (
            <tr key={b.id}>
              <td>{b.equipmentName ?? `#${b.equipmentId}`}</td>

              {/* Quantity fallback */}
              <td>{b.quantity ?? b.quantityRequested ?? "-"}</td>

              <td>
                {new Date(b.startAt).toLocaleString()} →{" "}
                {new Date(b.endAt).toLocaleString()}
              </td>

              <td>
                <span className={`status status-${b.status}`}>{b.status}</span>
              </td>

              {/* Requested by fallback */}
              <td>{b.requestedBy?.username ?? b.requesterUsername ?? "-"}</td>

              {isAdmin && (
                <td>
                  {b.status === "PENDING" ? (
                    <>
                      <button
                        onClick={() => onApprove?.(b.id)}
                        className="btn-small"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => onReject?.(b.id)}
                        className="btn-ghost"
                      >
                        Reject
                      </button>
                    </>
                  ) : (
                    <em>{b.status}</em>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
