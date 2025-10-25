import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.response.use(
  (res) => res,
  (err) => Promise.reject(err)
);
