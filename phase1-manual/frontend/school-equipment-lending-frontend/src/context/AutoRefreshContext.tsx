// src/context/AutoRefreshContext.tsx
import React, { createContext, useContext, useEffect, useState } from "react";

type RefreshContextType = {
  lastRefresh: number;
};

const AutoRefreshContext = createContext<RefreshContextType>({ lastRefresh: Date.now() });

export const AutoRefreshProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [lastRefresh, setLastRefresh] = useState(Date.now());

  useEffect(() => {
    const interval = setInterval(() => {
      setLastRefresh(Date.now()); // triggers refresh every second
    }, 1000); // 1000ms = 1s

    return () => clearInterval(interval);
  }, []);

  return (
    <AutoRefreshContext.Provider value={{ lastRefresh }}>
      {children}
    </AutoRefreshContext.Provider>
  );
};

export const useAutoRefresh = () => useContext(AutoRefreshContext);
