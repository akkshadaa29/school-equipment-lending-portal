import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

type Props = {
  children: React.ReactNode;
};

export const ProtectedRoute: React.FC<Props> = ({ children }) => {
  const { token } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

export const AdminRoute: React.FC<Props> = ({ children }) => {
  const { token, hasRole } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  if (!hasRole("ROLE_ADMIN")) return <Navigate to="/" replace />;
  return <>{children}</>;
};