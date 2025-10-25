// src/pages/MyLoans.tsx
import React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getMyLoans, returnLoan } from "../service/booking";
import type { Loan } from "../service/booking";
import { toast } from "react-hot-toast";

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
      qc.invalidateQueries({ queryKey: ["equipments"] }); // refresh availability
      qc.invalidateQueries({ queryKey: ["bookings", "mine"] }); // if bookings depend on loans
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
    <div>
      <h2 className="section-title">My Loans</h2>

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
            {loans.length === 0 && (
              <tr>
                <td colSpan={5} style={{ textAlign: "center", padding: "2rem" }}>
                  No loans found.
                </td>
              </tr>
            )}

            {loans.map((l) => (
              <tr key={l.id}>
                <td>{l.equipmentName ?? `#${l.equipmentId}`}</td>
                <td>{l.quantity}</td>
                <td>{new Date(l.dueAt).toLocaleString()}</td>
                <td>{l.status}</td>
                <td>
                  {l.status === "BORROWED" ? (
                    <button
                      className="btn-small"
                      disabled={mutation.isLoading}
                      onClick={() => {
                        // optional: confirm with user
                        if (!confirm("Mark this loan as returned?")) return;
                        mutation.mutate(l.id);
                      }}
                    >
                      {mutation.isLoading ? "Returning..." : "Return"}
                    </button>
                  ) : (
                    <span style={{ color: "#6b7280" }}>{l.status}</span>
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
