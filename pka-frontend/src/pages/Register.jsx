import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../services/authService";

export default function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ username: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const handleChange = (event) => setFormData({ ...formData, [event.target.name]: event.target.value });
  const handleSubmit = async (event) => {
    event.preventDefault(); setError(""); setBusy(true);
    try { await registerUser(formData); navigate("/"); }
    catch (err) { setError(err.response?.data?.message || "Registration failed. Please check your details."); }
    finally { setBusy(false); }
  };
  return <main className="auth-page">
    <section className="auth-story">
      <div className="brand-lockup"><div className="brand-mark" style={{ background: "#d9e8d9", color: "#1d4437" }}>✳</div><div><div className="brand-name" style={{ color: "#f6f2e8" }}>folio.</div><div className="brand-caption" style={{ color: "#c4d4c8" }}>your personal knowledge library</div></div></div>
      <div><span className="eyebrow" style={{ color: "#b6cdb9" }}>START WITH ONE GOOD THOUGHT</span><h1>Make a home<br />for what you learn.</h1><p>Build a private library for projects, questions, half-formed ideas, and the details you don't want to lose.</p></div>
      <span className="brand-caption" style={{ color: "#9eb6a4" }}>Your notes. Your thread. Your next idea.</span>
    </section>
    <section className="auth-form-wrap"><form className="auth-form" onSubmit={handleSubmit}>
      <span className="eyebrow">MAKE IT YOURS</span><h2>Create your library.</h2><p className="muted-copy">A few details and you're ready to start collecting ideas.</p>
      <div className="field"><label htmlFor="username">Your name</label><input id="username" type="text" name="username" autoComplete="name" placeholder="How should we call you?" value={formData.username} onChange={handleChange} required /></div>
      <div className="field"><label htmlFor="email">Email address</label><input id="email" type="email" name="email" autoComplete="email" placeholder="you@example.com" value={formData.email} onChange={handleChange} required /></div>
      <div className="field"><label htmlFor="password">Password</label><input id="password" type="password" name="password" autoComplete="new-password" placeholder="Create a password" value={formData.password} onChange={handleChange} required /></div>
      {error && <p className="notice notice-error" role="alert">{error}</p>}
      <button className="button button-primary" type="submit" disabled={busy}>{busy ? "Creating library…" : "Create account ↗"}</button>
      <p className="auth-foot">Already have a library? <Link to="/">Sign in</Link></p>
    </form></section>
  </main>;
}
