// src/components/ProtectedRoute.tsx
import { Navigate } from "react-router-dom";
import { getToken } from "../api/axios";
import * as React from "react";

interface ProtectedRouteProps {
  children: JSX.Element;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = getToken();
  if (!token) return <Navigate to="/login" replace />;
  return children;
}
