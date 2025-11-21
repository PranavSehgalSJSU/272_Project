"use client";

import api from "@/app/api";
import React, { useState } from "react";

const SIGNUP_URL = "http://localhost:8080/auth/signup";

export default function SignupPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [pushId, setPushId] = useState("");
  const [allowAlerts, setAllowAlerts] = useState(true);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (!username || !password || !email) {
      setError("Please fill username, email, and password.");
      return;
    }

    setIsLoading(true);
    try {
      const payload = { username, password, email, pushId, allowAlerts };
      await api.post<any>(SIGNUP_URL, payload, {
        withAuth: false,
        credentials: "omit", // change to "include" if server uses cookies
      });
      setMessage("Account created. You can sign in now.");
      window.location.assign("/auth/login");
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Signup failed";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="min-h-screen grid place-items-center bg-zinc-50 dark:bg-black p-4">
      <div className="w-full max-w-md rounded-2xl bg-white dark:bg-zinc-900 p-8 shadow-md">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">Create account</h1>
        <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">We'll only ask what we need.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">Username</label>
            <input id="username" type="text" value={username} onChange={(e) => setUsername(e.target.value)} required className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800" />
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">Email</label>
            <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800" />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">Password</label>
            <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800" />
          </div>

          <div>
            <label htmlFor="pushId" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">pushId</label>
            <input id="pushId" type="text" value={pushId} onChange={(e) => setPushId(e.target.value)} className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800" placeholder="optional" />
          </div>

          <label className="inline-flex items-center gap-2 text-sm text-zinc-800 dark:text-zinc-200">
            <input type="checkbox" className="rounded border-zinc-300" checked={allowAlerts} onChange={(e) => setAllowAlerts(e.target.checked)} />
            Allow alerts
          </label>

          <button type="submit" disabled={isLoading} className="w-full rounded-xl bg-black px-4 py-2 text-white transition disabled:opacity-60">
            {isLoading ? "Creating account…" : "Create account"}
          </button>

          {error && <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-400/40 dark:bg-red-900/30 dark:text-red-200">{error}</div>}
          {message && <div className="rounded-xl border border-green-200 bg-green-50 p-3 text-sm text-green-700 dark:border-green-400/40 dark:bg-green-900/30 dark:text-green-200">{message}</div>}
        </form>

        <div className="mt-6 text-sm text-zinc-600 dark:text-zinc-400">
          Already have an account? <a href="/auth/login" className="font-medium underline">Sign in</a>
        </div>
      </div>
    </div>
  );
}
