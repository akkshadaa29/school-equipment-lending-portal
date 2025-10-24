// src/services/booking.ts
import { api } from "../lib/axios";

export type Booking = {
  id: number;
  equipmentId: number;
  equipmentName?: string;
  quantity: number;
  startAt: string; // ISO
  endAt: string; // ISO
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt?: string;
  requestedBy?: { id?: number; username?: string };
};

export type Loan = {
  id: number;
  equipmentId: number;
  equipmentName?: string;
  quantity: number;
  borrowedAt: string; // ISO
  dueAt: string; // ISO
  returnedAt?: string | null;
  status: "BORROWED" | "RETURNED" | "OVERDUE";
};

export const createBooking = async (payload: {
  equipmentId: number;
  quantity: number;
  startAt: string;
  endAt: string;
}) => {
  const res = await api.post<Booking>("/bookings", payload);
  return res.data;
};

export const getMyBookings = async () => {
  const res = await api.get<Booking[]>("/bookings/my");
  return res.data;
};

export const getPendingBookings = async () => {
  const res = await api.get<Booking[]>("/bookings/pending");
  return res.data;
};

export const approveBooking = async (bookingId: number) => {
  const res = await api.post(`/bookings/${bookingId}/approve`);
  return res.data;
};

export const rejectBooking = async (bookingId: number) => {
  const res = await api.post(`/bookings/${bookingId}/reject`);
  return res.data;
};

export const getMyLoans = async () => {
  const res = await api.get<Loan[]>("/loans/my");
  return res.data;
};

export const returnLoan = async (loanId: number) => {
  const res = await api.post(`/loans/${loanId}/return`);
  return res.data;
};
