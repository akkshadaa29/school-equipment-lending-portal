// src/main.tsx
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { AuthProvider } from "./context/AuthContext";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { Toaster } from "react-hot-toast";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false, // Disable automatic retries to prevent duplicate requests or toasts
      refetchOnWindowFocus: false, // Do not refetch when window gains focus
    },
  },
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          {/* Toast notifications (rendered globally once) */}
          <Toaster
            position="top-center"
            reverseOrder={false}
            toastOptions={{
              duration: 3000,
              style: { fontSize: "0.95rem" },
              success: {
                iconTheme: {
                  primary: "#22c55e", // green
                  secondary: "#fff",
                },
              },
              error: {
                iconTheme: {
                  primary: "#ef4444", // red
                  secondary: "#fff",
                },
              },
            }}
          />
          <App />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
