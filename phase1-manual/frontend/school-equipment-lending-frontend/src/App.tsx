// src/App.tsx
import React from "react";
import { Routes, Route } from "react-router-dom";

import Layout from "./components/Layout";
import Dashboard from "./pages//Dashboard";
import Login from "./pages//Login";
import Register from "./pages//Register";
import AdminBookings from "./pages/AdminBookings";
import AdminEquipment from "./pages/AdminEquipment";
import { ProtectedRoute, AdminRoute } from "./components/ProtectedRoute";

/* Pages (bookings and request pages) */
import BookingsPage from "./pages/Bookings";
import RequestEquipmentPage from "./pages/RequestEquipmentPage";

const App: React.FC = () => {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/bookings"
        element={
          <ProtectedRoute>
            <Layout>
              <BookingsPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/request"
        element={
          <ProtectedRoute>
            <Layout>
              <RequestEquipmentPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/loans"
        element={
          <ProtectedRoute>
            <Layout>
              <div style={{ padding: 24 }}>My Loans page (coming soon)</div>
            </Layout>
          </ProtectedRoute>
        }
      />

      {/* Admin-only */}
      <Route
        path="/admin/bookings"
        element={
          <AdminRoute>
            <Layout>
              <AdminBookings />
            </Layout>
          </AdminRoute>
        }
      />
   

<Route
  path="/admin/equipment"
  element={
    <AdminRoute>
      <Layout>
        <AdminEquipment />
      </Layout>
    </AdminRoute>
  }
/>

      {/* 404 fallback */}
      <Route
        path="*"
        element={
          <Layout>
            <div style={{ padding: "2rem" }}>
              <h2>404 - Page Not Found</h2>
              <p>Try navigating via the sidebar or go back to Dashboard.</p>
            </div>
          </Layout>
        }
      />
    </Routes>
  );
};

export default App;
