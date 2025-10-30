import React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getMyLoans, returnLoan } from "../service/booking";
import type { Loan } from "../service/booking";
import { toast } from "react-hot-toast";
import "./MyLoans.css"; // <-- local, scoped CSS for loans page

const MyLoansPage: React.FC = () => {
  const qc = useQueryClient();

  const { data: loans = [], isLoading, isError } = useQuery<Loan[]>({
    queryKey: ["loans", "me"],
    queryFn: getMyLoans,
    staleTime: 60_000,
  });

  const mutation = useMutation({
    mutationFn: (loanId: number) => returnLoan(loanId),
    onSuccess: () => {
      toast.success("Returned successfully");
      qc.invalidateQueries({ queryKey: ["loans", "me"] });
      qc.invalidateQueries({ queryKey: ["equipments"] });
      qc.invalidateQueries({ queryKey: ["bookings", "mine"] });
    },
    onError: (err: any) => {
      console.error("Return failed:", err);
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        "Failed to return loan";
      toast.error(msg);
    },
    retry: false,
  });

  if (isLoading) return <div className="card">Loading loans…</div>;
  if (isError) return <div className="card">Failed to load loans.</div>;

  return (
    <div className="my-loans-page">
      <h2 className="section-title">My Loans</h2>

      <div className="card table-card">
        <table className="equip-table loan-table" aria-label="My loans">
          <thead>
            <tr>
              <th className="col-equipment">Equipment</th>
              <th className="col-qty">Qty</th>
              <th className="col-range">Due Date</th>
              <th className="col-status">Status</th>
              <th className="col-actions">Action</th>
            </tr>
          </thead>
          <tbody>
            {loans.length === 0 && (
              <tr>
                <td colSpan={5} style={{ textAlign: "center", padding: "2rem" }}>
                  No loans found.
                </td>
              </tr>
            )}

            {loans.map((l) => (
              <tr key={l.id}>
                <td className="equip-cell" data-label="Equipment">
                  {l.equipmentName ?? `#${l.equipmentId}`}
                </td>

                <td className="qty-cell" data-label="Qty" style={{ textAlign: "center" }}>
                  {l.quantity}
                </td>

                <td className="range-cell" data-label="Due Date" style={{ textAlign: "center" }}>
                  {new Date(l.dueAt).toLocaleDateString()}
                </td>

                <td className="status-cell" data-label="Status" style={{ textAlign: "center" }}>
                  <span
                    className={`status status-${(l.status || "default").toString().toLowerCase()}`}
                  >
                    {l.status}
                  </span>
                </td>

                <td className="actions-cell" data-label="Action" style={{ textAlign: "right" }}>
                  {l.status === "BORROWED" ? (
                    <button
                      className="btn-small loan-return-btn"
                      disabled={mutation.isLoading}
                      onClick={() => {
                        if (!confirm("Mark this loan as returned?")) return;
                        mutation.mutate(l.id);
                      }}
                    >
                      {mutation.isLoading ? "Returning..." : "Return"}
                    </button>
                  ) : (
                    <span className="action-text">{l.status}</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default MyLoansPage;
