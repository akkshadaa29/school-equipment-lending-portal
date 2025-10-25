import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Register: React.FC = () => {
  const { register: registerUser, isLoading } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);

    if (password !== confirm) {
      setErr("Passwords do not match.");
      return;
    }

    try {
      await registerUser(username.trim(), password);
      nav("/login");
    } catch (error: unknown) {
      console.error("Registration error:", error);

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
        setErr("Registration failed. Please try again.");
      }
    }
  };

  return (
    <div className="auth-container">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Create Account</h2>

        {err && <div className="auth-error" role="alert">{err}</div>}

        <input
          className="auth-input"
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />

        <input
          className="auth-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <input
          className="auth-input"
          type="password"
          placeholder="Confirm Password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          required
        />

        <button
          className="auth-btn"
          type="submit"
          disabled={isLoading}
          aria-busy={isLoading}
        >
          {isLoading ? "Creating..." : "Sign Up"}
        </button>

        <div className="auth-footer">
          <span>Already have an account? </span>
          <Link to="/login">Sign in</Link>
        </div>
      </form>
    </div>
  );
};

export default Register;
