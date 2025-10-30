import React from "react";
import type { Booking } from "../service/booking";
import "./BookingList.css";

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

  const fmtDate = (v?: string | number | Date) => {
    if (!v) return "-";
    const d = new Date(v);
    if (Number.isNaN(d.getTime())) return "-";
    // show only the date (localized). Adjust options if you want "Jan 1, 2025" style.
    return d.toLocaleDateString();
  };

  return (
    // scroll-wrapper provides the internal scrollbar and fixed header effect
    <div className="booking-table-wrapper">
      <table className="equip-table">
        <thead>
          <tr>
            <th className="col-equipment">Equipment</th>
            <th className="col-qty">Quantity</th>
            <th className="col-range">Duration</th>
            <th className="col-status">Status</th>
            <th className="col-requested">Requested by</th>
            {isAdmin && <th className="col-actions">Actions</th>}
          </tr>
        </thead>

        <tbody>
          {bookings.map((b) => (
            <tr key={b.id}>
              <td className="equip-cell">{b.equipmentName ?? `#${b.equipmentId}`}</td>

              <td className="qty-cell">{b.quantity ?? b.quantityRequested ?? "-"}</td>

              <td className="range-cell">
                <div className="range-line">
                  <div>{fmtDate(b.startAt)}</div>
                  <div className="arrow">→</div>
                  <div>{fmtDate(b.endAt)}</div>
                </div>
              </td>

              <td className="status-cell">
                <span className={`status status-${b.status?.toLowerCase()}`}>
                  {b.status}
                </span>
              </td>

              <td className="requested-cell">
                {b.requestedBy?.username ?? b.requesterUsername ?? "-"}
              </td>

              {isAdmin && (
                <td className="actions-cell">
                  {b.status === "PENDING" ? (
                    <div className="action-buttons">
                      <button
                        onClick={() => onApprove?.(b.id)}
                        className="btn-small btn-approve"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => onReject?.(b.id)}
                        className="btn-small btn-reject"
                      >
                        Reject
                      </button>
                    </div>
                  ) : (
                    <em className="muted">{b.status}</em>
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
