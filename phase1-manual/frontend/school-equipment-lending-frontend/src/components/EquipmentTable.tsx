// src/components/EquipmentTable.tsx
import React from "react";
import type { Equipment } from "../service/equipment";

const ConditionPill: React.FC<{ condition: string }> = ({ condition }) => {
  const c = condition?.toLowerCase?.() ?? "";
  let cls = "pill ";
  if (c.includes("excellent")) cls += "pill-excellent";
  else if (c.includes("good")) cls += "pill-good";
  else if (c.includes("poor") || c.includes("bad")) cls += "pill-poor";
  else cls += "pill-fair";
  return <span className={cls}>{condition}</span>;
};

export const EquipmentTable: React.FC<{ rows: Equipment[] }> = ({ rows }) => {
  if (!rows?.length) {
    return <div className="card table-card">No equipment found.</div>;
  }

  return (
    <div className="card table-card">
      <div className="table-scroll">
        <table className="equip-table" role="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Condition</th>
              <th>Available Units</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.name}</td>
                <td>{r.category}</td>
                <td>
                  <ConditionPill condition={r.condition ?? "Unknown"} />
                </td>
                <td>
                  {typeof r.availableUnits === "number"
                    ? r.availableUnits
                    : r.quantity ?? "-"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
export default EquipmentTable;