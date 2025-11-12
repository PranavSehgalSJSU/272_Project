"use client";

import api from "@/app/api";
import React, { useState } from "react";

const LOGIN_URL = "http://localhost:8080/auth/login";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");

    if (!username || !password) {
      setError("Please enter username and password.");
      return;
    }

    setIsLoading(true);
    try {
      const payload = { username, password };
      const data = await api.post<any>(LOGIN_URL, payload, {
        withAuth: false,
        credentials: "omit", // change to "include" if your API uses cookies/sessions
      });

      // Optional: if your API returns a token, save it using setToken from api.ts
      // import { setToken } from "@/api"; if (data?.token) setToken(data.token);

      // Redirect to your app after login
      window.location.assign("/home");
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Login failed";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="min-h-screen grid place-items-center bg-zinc-50 dark:bg-black p-4">
      <div className="w-full max-w-md rounded-2xl bg-white dark:bg-zinc-900 p-8 shadow-md">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">Sign in</h1>
        <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">Use your username and password.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
              className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800"
              placeholder="Disha4"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-zinc-800 dark:text-zinc-200">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
              className="mt-1 w-full rounded-xl border border-zinc-300 bg-white/90 px-3 py-2 outline-none focus:ring-2 focus:ring-black dark:border-zinc-700 dark:bg-zinc-800"
              placeholder="••••"
            />
          </div>

          <button type="submit" disabled={isLoading} className="w-full rounded-xl bg-black px-4 py-2 text-white transition disabled:opacity-60">
            {isLoading ? "Signing in…" : "Sign in"}
          </button>

          {error && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-400/40 dark:bg-red-900/30 dark:text-red-200">
              {error}
            </div>
          )}
        </form>

        <div className="mt-6 text-sm text-zinc-600 dark:text-zinc-400">
          Don't have an account? <a href="/auth/signup" className="font-medium underline">Create one</a>
        </div>
      </div>
    </div>
  );
}
