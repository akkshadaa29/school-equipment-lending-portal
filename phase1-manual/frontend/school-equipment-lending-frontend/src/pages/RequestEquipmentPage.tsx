// src/pages/RequestEquipment.tsx
import React from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchEquipments } from "../service/equipment";
import { BookingForm } from "../components/BookingForm";

const RequestEquipmentPage: React.FC = () => {
  const qc = useQueryClient();

  const { data: equipments = [], isLoading, isError } = useQuery({
    queryKey: ["equipments", { for: "request" }],
    queryFn: () =>
      fetchEquipments({
        // no filters for the form — fetch full list
      }),
    staleTime: 60_000,
  });

  return (
    <div>
      <h2 className="section-title">Request equipment</h2>

      {isLoading && <div className="card">Loading equipment…</div>}
      {isError && <div className="card">Failed to load equipment.</div>}

      {!isLoading && !isError && (
        <div className="card request-card">
          <BookingForm
            equipments={equipments}
            onSuccess={() => {
              // Refresh equipment + bookings lists after a new request
              qc.invalidateQueries({ queryKey: ["equipments"] });
              qc.invalidateQueries({ queryKey: ["bookings", "me"] });
            }}
          />
        </div>
      )}
    </div>
  );
};

export default RequestEquipmentPage;