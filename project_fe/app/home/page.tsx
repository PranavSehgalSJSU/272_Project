"use client";
import React, { useState, useEffect } from "react";

const ALERT_URL = "http://localhost:8080/alert/sendFromUser";

export default function Page() {
  const [form, setForm] = useState({
    sender: "",
    receiver: "",
    mode: "both" as "both" | "email" | "sms",
    header: "",
    message: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const savedUser = localStorage.getItem("userName") || "";
    setForm((f) => ({ ...f, sender: savedUser }));
  }, []);

  const canSubmit = form.sender && form.receiver && form.message;

  function buildJSON() {
    return {
      sender: form.sender,
      receiver: form.receiver.trim(),
      message: form.message,
      header: form.header,
      mode: form.mode === "both" ? "*" : form.mode,
      token: localStorage.getItem("token") || "",
    };
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!canSubmit) {
      setError("Please fill required fields.");
      return;
    }

    setLoading(true);

    try {
      const jsonBody = buildJSON();

      const res = await fetch(ALERT_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(jsonBody),
      });

      if (!res.ok) throw new Error(`Server error: ${res.status}`);

      const text = await res.text();
      setSuccess(text || "Alert sent!");
    } catch (err: any) {
      setError(err.message || "Failed to send alert");
    }

    setLoading(false);
  }

  return (
    <div className="min-h-screen grid place-items-center bg-zinc-50 dark:bg-black p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white dark:bg-zinc-900 p-8 shadow-md">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100 text-center">
          Send Alert
        </h1>

        <form onSubmit={onSubmit} className="mt-6 space-y-5">
          <div>
            <label className="block text-sm font-medium">Sender</label>
            <input
              className="mt-1 w-full rounded-xl border px-3 py-2"
              value={form.sender}
              readOnly
            />
          </div>

          <div>
            <label className="block text-sm font-medium">Receiver</label>
            <input
              className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="bob"
              value={form.receiver}
              onChange={(e) =>
                setForm((f) => ({ ...f, receiver: e.target.value }))
              }
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium">Mode</label>
            <div className="flex gap-3 mt-1">
              {(["both", "email", "sms"] as const).map((m) => (
                <label
                  key={m}
                  className={`inline-flex items-center gap-2 border rounded-xl px-3 py-2 cursor-pointer ${
                    form.mode === m ? "ring-2 ring-black" : ""
                  }`}
                >
                  <input
                    type="radio"
                    name="mode"
                    value={m}
                    checked={form.mode === m}
                    onChange={(e) =>
                      setForm((f) => ({ ...f, mode: e.target.value as any }))
                    }
                  />
                  <span className="capitalize">{m}</span>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium">Header</label>
            <input
              className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="Optional title for Email"
              value={form.header}
              onChange={(e) =>
                setForm((f) => ({ ...f, header: e.target.value }))
              }
            />
          </div>

          <div>
            <label className="block text-sm font-medium">Message</label>
            <textarea
              rows={5}
              className="mt-1 w-full rounded-xl border px-3 py-2"
              placeholder="Write your message..."
              value={form.message}
              onChange={(e) =>
                setForm((f) => ({ ...f, message: e.target.value }))
              }
              required
            />
          </div>

          <button
            type="submit"
            disabled={!canSubmit || loading}
            className="w-full rounded-xl bg-black px-4 py-2 text-white font-semibold"
          >
            {loading ? "Sending…" : "Send Alert"}
          </button>
        </form>

        {(error || success) && (
          <div className="mt-6 space-y-3">
            {error && (
              <div className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-800">
                {error}
              </div>
            )}
            {success && (
              <div className="rounded-xl border border-green-300 bg-green-50 p-4 text-green-800">
                {success}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
