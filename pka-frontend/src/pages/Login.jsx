import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { loginUser } from "../services/authService";

export default function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const handleChange = (event) => setFormData({ ...formData, [event.target.name]: event.target.value });
  const handleSubmit = async (event) => {
    event.preventDefault(); setError(""); setBusy(true);
    try { const response = await loginUser(formData); localStorage.setItem("token", response.token); navigate("/dashboard"); }
    catch (err) { setError(err.response?.data?.message || "We couldn't sign you in. Check your email and password."); }
    finally { setBusy(false); }
  };
  return <main className="auth-page">
    <section className="auth-story">
      <div className="brand-lockup"><div className="brand-mark" style={{ background: "#d9e8d9", color: "#1d4437" }}>✳</div><div><div className="brand-name" style={{ color: "#f6f2e8" }}>folio.</div><div className="brand-caption" style={{ color: "#c4d4c8" }}>your personal knowledge library</div></div></div>
      <div><span className="eyebrow" style={{ color: "#b6cdb9" }}>A QUIETER PLACE FOR BIG IDEAS</span><h1>Your thoughts,<br />with room to grow.</h1><p>Bring your notes together. Follow the questions that matter. Let your own knowledge lead you somewhere new.</p></div>
      <span className="brand-caption" style={{ color: "#9eb6a4" }}>A little space to think clearly.</span>
    </section>
    <section className="auth-form-wrap"><form className="auth-form" onSubmit={handleSubmit}>
      <span className="eyebrow">WELCOME BACK</span><h2>Pick up where you left off.</h2><p className="muted-copy">Sign in to return to your notes and conversations.</p>
      <div className="field"><label htmlFor="email">Email address</label><input id="email" type="email" name="email" autoComplete="email" placeholder="you@example.com" value={formData.email} onChange={handleChange} required /></div>
      <div className="field"><label htmlFor="password">Password</label><input id="password" type="password" name="password" autoComplete="current-password" placeholder="Your password" value={formData.password} onChange={handleChange} required /></div>
      {error && <p className="notice notice-error" role="alert">{error}</p>}
      <button className="button button-primary" type="submit" disabled={busy}>{busy ? "Signing in…" : "Sign in ↗"}</button>
      <p className="auth-foot">New to folio? <Link to="/register">Create your library</Link></p>
    </form></section>
  </main>;
}
