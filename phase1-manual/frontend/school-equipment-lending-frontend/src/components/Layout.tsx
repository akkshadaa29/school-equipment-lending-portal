import React from "react";
import Sidebar from "./Sidebar"

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <div className="app-root">
    <Sidebar />
    <main className="main-area">
      <div className="page-shell">{children}</div>
    </main>
  </div>
);

export default Layout;
