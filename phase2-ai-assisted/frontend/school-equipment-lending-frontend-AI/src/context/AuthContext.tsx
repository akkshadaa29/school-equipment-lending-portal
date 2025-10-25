// src/context/AuthContext.tsx
import React, { createContext, useContext, useEffect, useState } from "react";
import { api } from "../lib/axios";

// ====== Types ======
type Role = "ROLE_USER" | "ROLE_ADMIN" | "ROLE_STAFF";
type User = { id?: number; username?: string; roles?: Role[] } | null;

interface AuthContextValue {
  user: User;
  token: string | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: Role) => boolean;
}

// ====== Context ======
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// ====== Hook ======
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};

// ====== Provider ======
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => {
    try {
      const t = localStorage.getItem("token");
      if (!t || t === "undefined" || t.trim() === "") {
        localStorage.removeItem("token");
        return null;
      }
      return t;
    } catch {
      return null;
    }
  });

  const [user, setUser] = useState<User>(() => {
    try {
      const s = localStorage.getItem("user");
      if (!s || s === "undefined" || s.trim() === "") {
        localStorage.removeItem("user");
        return null;
      }
      return JSON.parse(s) as User;
    } catch {
      localStorage.removeItem("user");
      return null;
    }
  });

  const [isLoading, setIsLoading] = useState(false);

  // Attach token to axios automatically
  useEffect(() => {
    if (token) {
      api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common["Authorization"];
    }
  }, [token]);

  // ====== Fetch Current User ======
  const fetchMe = async () => {
    try {
      const resp = await api.get("/auth/me");
      const me = resp.data;
      if (me) {
        setUser(me);
        localStorage.setItem("user", JSON.stringify(me));
      }
    } catch (err: any) {
      if (err?.response?.status === 401) {
        logout();
      } else {
        console.warn("fetchMe failed:", err);
      }
    }
  };

  // ====== Rehydrate user on mount ======
  useEffect(() => {
    if (token && !user) {
      fetchMe().catch(() => {});
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ====== LOGIN ======
  const login = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await api.post("/auth/login", { username, password });
      const data = res.data || {};

      const accessToken = data.accessToken ?? data.token ?? data.jwt ?? null;
      const returnedUser = data.user ?? null;

      if (!accessToken) throw new Error("Login response did not contain an access token.");

      // Store token
      setToken(accessToken);
      api.defaults.headers.common["Authorization"] = `Bearer ${accessToken}`;
      localStorage.setItem("token", accessToken);

      if (returnedUser) {
        setUser(returnedUser);
        localStorage.setItem("user", JSON.stringify(returnedUser));
      } else {
        localStorage.removeItem("user");
        setUser(null);
        await fetchMe();
      }
    } catch (err: any) {
      console.error("Login failed:", err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  // ====== REGISTER ======
  const register = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      await api.post("/auth/signup", { username, password });
      await login(username, password);
    } catch (err: any) {
      console.error("Register failed:", err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  // ====== LOGOUT ======
  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    delete api.defaults.headers.common["Authorization"];
  };

  // ====== ROLE CHECK ======
  const hasRole = (role: Role) => Boolean(user?.roles?.includes(role));

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, hasRole, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};
