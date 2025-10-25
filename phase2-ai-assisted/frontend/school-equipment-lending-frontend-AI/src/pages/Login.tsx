import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Login: React.FC = () => {
  const { login, isLoading } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);

    try {
      await login(username.trim(), password);
      nav("/");
    } catch (error: unknown) {
      console.error("Login error:", error);

      if (
        typeof error === "object" &&
        error !== null &&
        "response" in error &&
        (error as any).response?.data?.message
      ) {
        setErr((error as any).response.data.message);
      } else if (error instanceof Error) {
        setErr(error.message);
      } else {
        setErr("Login failed. Please try again.");
      }
    }
  };

  return (
    <div className="auth-container">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Sign In</h2>

        {err && <div className="auth-error" role="alert">{err}</div>}

        <input
          className="auth-input"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
          required
        />

        <input
          className="auth-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
        />

        <button
          className="auth-btn"
          type="submit"
          disabled={isLoading}
          aria-busy={isLoading}
        >
          {isLoading ? "Signing in..." : "Sign In"}
        </button>

        <div className="auth-footer">
          <span>New user? </span>
          <Link to="/register">Create account</Link>
        </div>
      </form>
    </div>
  );
};

export default Login;
