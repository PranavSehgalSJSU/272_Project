"use client";
import React, { useState } from "react";
import api from "@/app/api";

const ALERT_URL = "http://localhost:8080/alert/sendFromUser";

export default function Page() {
  const [form, setForm] = useState({
    sender: "",
    receiverInput: "", // single username, comma-separated list, or "*"
    mode: "both" as "both" | "email" | "sms",
    header: "",
    message: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const canSubmit = form.sender && form.message && form.receiverInput;

  function buildPayload() {
    const body: any = {
      sender: form.sender,
      message: form.message,
    };

    // Mode mapping: both => "*"; email/sms stay as-is
    body.mode = form.mode === "both" ? "*" : form.mode;

    if (form.header) body.header = form.header;

    const raw = form.receiverInput.trim();
    if (raw === "*") {
      body.receiver = "*"; // all allowAlerts=true users
    } else if (raw.includes(",")) {
      body.receivers = raw.split(",").map((s) => s.trim()).filter(Boolean);
    } else {
      body.receiver = raw; // single username
    }

    return body;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setMessage("");

    if (!canSubmit) {
      setError("Please fill required fields.");
      return;
    }

    setLoading(true);
    try {
      const payload = buildPayload();
      const res = await api.post<any>(ALERT_URL, payload, {
        withAuth: true, // auth captured by login
        credentials: "include", // include cookies if your server uses them
      });

      const serverMsg =
        (typeof res === "string" && res) ||
        (res && (res.message || res.data || res.statusText)) ||
        "Alert request sent.";

      setMessage(serverMsg);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to send alert";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="mx-auto max-w-2xl">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold tracking-tight">Send Alert</h1>
          
        </div>

        <form onSubmit={onSubmit} className="grid gap-5 bg-white shadow-sm rounded-2xl p-6">
          <div className="grid gap-2">
            <label className="font-medium" htmlFor="sender">Sender Username</label>
            <input
              id="sender"
              className="border rounded-xl px-3 py-2 focus:outline-none focus:ring w-full"
              placeholder="alice"
              value={form.sender}
              onChange={(e) => setForm((f) => ({ ...f, sender: e.target.value }))}
              required
            />
          </div>

          <div className="grid gap-2">
            <label className="font-medium" htmlFor="receivers">Receiver(s)</label>
            <input
              id="receivers"
              className="border rounded-xl px-3 py-2 focus:outline-none focus:ring w-full"
              placeholder="bob  •  bob,alice"
              value={form.receiverInput}
              onChange={(e) => setForm((f) => ({ ...f, receiverInput: e.target.value }))}
              required
            />
            
          </div>

          <div className="grid gap-2">
            <label className="font-medium">Mode</label>
            <div className="flex gap-3 flex-wrap">
              {(["both", "email", "sms"] as const).map((m) => (
                <label
                  key={m}
                  className={`inline-flex items-center gap-2 border rounded-2xl px-3 py-2 cursor-pointer ${
                    form.mode === m ? "ring-2 ring-black" : ""
                  }`}
                >
                  <input
                    type="radio"
                    className="accent-black"
                    name="mode"
                    value={m}
                    checked={form.mode === m}
                    onChange={(e) => setForm((f) => ({ ...f, mode: e.target.value as any }))}
                  />
                  <span className="capitalize">{m}</span>
                </label>
              ))}
            </div>
            
          </div>

          <div className="grid gap-2">
            <label className="font-medium" htmlFor="header">Email Header (optional)</label>
            <input
              id="header"
              className="border rounded-xl px-3 py-2 focus:outline-none focus:ring w-full"
              placeholder="e.g. IMPORTANT"
              value={form.header}
              onChange={(e) => setForm((f) => ({ ...f, header: e.target.value }))}
            />
            
          </div>

          <div className="grid gap-2">
            <label className="font-medium" htmlFor="message">Message</label>
            <textarea
              id="message"
              rows={5}
              className="border rounded-xl px-3 py-2 focus:outline-none focus:ring w-full"
              placeholder="Write your alert message..."
              value={form.message}
              onChange={(e) => setForm((f) => ({ ...f, message: e.target.value }))}
              required
            />
          </div>

          <div className="flex items-center justify-between gap-4">
            <button
              type="submit"
              disabled={!canSubmit || loading}
              className="rounded-2xl px-5 py-2.5 font-semibold shadow-sm bg-black text-white disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Sending..." : "Send Alert"}
            </button>
            <button
              type="button"
              onClick={() => {
                setForm({ sender: "", receiverInput: "", mode: "both", header: "", message: "" });
                setError("");
                setMessage("");
              }}
              className="rounded-2xl px-4 py-2 font-medium border"
            >
              Reset
            </button>
          </div>
        </form>

        {(error || message) && (
          <div className="mt-6 space-y-3">
            {error && (
              <div className="rounded-2xl border border-red-300 bg-red-50 p-4 text-red-800">{error}</div>
            )}
            {message && (
              <div className="rounded-2xl border border-green-300 bg-green-50 p-4 text-green-800">{message}</div>
            )}
          </div>
        )}

        
      </div>
    </div>
  );
}
