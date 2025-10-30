import { api } from "../lib/axios";

export type Equipment = {
  id: number;
  name: string;
  category: string;
  available?: boolean;
  quantity?: number;
  condition?: string; // ✅ added
  createdAt?: string;
};

/**
 * Fetch equipments from backend.
 *
 * If any filter/search is provided, calls:
 *   GET /api/equipments/search?q=...&category=...&available=true|false
 *
 * Otherwise calls:
 *   GET /api/equipments
 */
export const fetchEquipments = async (params?: {
  q?: string;
  category?: string;
  available?: boolean;
}) => {
  const q = params?.q?.trim();
  const category = params?.category?.trim();
  const hasFilters = Boolean(q || category || typeof params?.available === "boolean");

  if (hasFilters) {
    const query: Record<string, string> = {};
    if (q) query.q = q;
    if (category) query.category = category;
    if (typeof params?.available === "boolean") query.available = String(params.available);

    const qs = new URLSearchParams(query).toString();
    const url = `/equipments/search${qs ? `?${qs}` : ""}`;
    const res = await api.get<Equipment[]>(url);
    return res.data;
  } else {
    const res = await api.get<Equipment[]>("/equipments");
    return res.data;
  }
};

/**
 * Create a new equipment (Admin only)
 * POST /api/equipments
 * Payload: { name, category, available, quantity, condition }
 */
export const createEquipment = async (payload: {
  name: string;
  category: string;
  available: boolean;
  quantity: number;
  condition: string; // ✅ added
}) => {
  const res = await api.post("/equipments", payload, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
    },
  });
  return res.data;
};

/**
 * Update an existing equipment (Admin only)
 * PUT /api/equipments/{id}
 * Payload may include: { name, category, available, quantity, condition }
 */
export const updateEquipment = async (
  id: number,
  payload: Partial<{
    name: string;
    category: string;
    available: boolean;
    quantity: number;
    condition: string; // ✅ added
  }>
) => {
  const res = await api.put(`/equipments/${id}`, payload, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
    },
  });
  return res.data;
};

/**
 * Delete an equipment (Admin only)
 * DELETE /api/equipments/{id}
 */
export const deleteEquipment = async (id: number) => {
  const res = await api.delete(`/equipments/${id}`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
    },
  });
  return res.data;
};
