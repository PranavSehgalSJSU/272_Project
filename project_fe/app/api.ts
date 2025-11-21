// api.ts — simplified, TypeScript-safe backend client
// This version avoids tricky env typings and compiles in Next.js, Vite, CRA.
// Drop in at: src/api.ts

export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

export interface RequestOptions {
  method?: HttpMethod;
  body?: unknown; // object, FormData, string, etc.
  headers?: Record<string, string>;
  timeout?: number; // ms
  withAuth?: boolean; // attach Authorization: Bearer <token>
  credentials?: RequestCredentials; // default: "include" for cookie sessions
}

export interface ApiError extends Error {
  status?: number;
  data?: unknown;
}

/** Token helpers (SSR-safe) */
const TOKEN_KEY = "auth_token";
export function getToken(): string | null {
  try { return typeof window !== "undefined" ? window.localStorage.getItem(TOKEN_KEY) : null; } catch { return null; }
}
export function setToken(token: string | null): void {
  try { if (typeof window === "undefined") return; token ? window.localStorage.setItem(TOKEN_KEY, token) : window.localStorage.removeItem(TOKEN_KEY); } catch {}
}
export function clearToken(): void { setToken(null); }

/** Base URL detection that works across toolchains without extra types */
export const BASE_URL: string =
  (typeof window !== "undefined" && (window as any).__API_BASE__) ||
  (typeof process !== "undefined" && (process as any).env?.NEXT_PUBLIC_API_URL) ||
  (typeof import.meta !== "undefined" && (import.meta as any).env?.VITE_API_URL) ||
  ""; // same-origin by default

/** Core request helper */
export async function request<T = unknown>(path: string, opts: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, headers = {}, timeout = 20000, withAuth = true, credentials = "include" } = opts;

  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);

  const url = /^https?:\/\//i.test(path) ? path : `${BASE_URL}${path}`;

  const finalHeaders: Record<string, string> = { ...headers };
  let finalBody: BodyInit | undefined;

  // JSON-encode plain objects by default; let FormData, strings, Blobs pass through
  if (body instanceof FormData || body instanceof Blob) {
    finalBody = body as BodyInit;
  } else if (typeof body === "string") {
    finalBody = body as BodyInit;
  } else if (body && typeof body === "object") {
    finalHeaders["Content-Type"] = finalHeaders["Content-Type"] || "application/json";
    finalBody = JSON.stringify(body);
  }

  if (withAuth) {
    const token = getToken();
    if (token) finalHeaders["Authorization"] = `Bearer ${token}`;
  }

  const init: RequestInit = { method, headers: finalHeaders, signal: controller.signal, credentials };
  if (finalBody !== undefined) init.body = finalBody;

  let res: Response;
  try {
    res = await fetch(url, init);
  } catch (err: unknown) {
    clearTimeout(id);
    const e: ApiError = Object.assign(new Error((err as Error)?.message || "Network error"), { status: 0 });
    throw e;
  }

  clearTimeout(id);

  const text = await res.text();
  let data: any = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text || null; }

  if (!res.ok) {
    const e: ApiError = Object.assign(new Error((data && (data.message || data.error || data.detail)) || res.statusText || "Request failed"), { status: res.status, data });
    throw e;
  }

  return data as T;
}

/** Convenience methods */
const api = {
  get:  <T = unknown>(path: string, opts?: RequestOptions) => request<T>(path, { ...opts, method: "GET" }),
  post: <T = unknown>(path: string, body?: unknown, opts?: RequestOptions) => request<T>(path, { ...opts, method: "POST", body }),
  put:  <T = unknown>(path: string, body?: unknown, opts?: RequestOptions) => request<T>(path, { ...opts, method: "PUT", body }),
  patch:<T = unknown>(path: string, body?: unknown, opts?: RequestOptions) => request<T>(path, { ...opts, method: "PATCH", body }),
  del:  <T = unknown>(path: string, opts?: RequestOptions) => request<T>(path, { ...opts, method: "DELETE" }),
};

/** Auth helpers */
export interface LoginPayload { email?: string; username?: string; password: string }
export interface LoginResponse { token?: string; [k: string]: unknown }

export const auth = {
  async login(payload: LoginPayload): Promise<LoginResponse> {
    const data = await api.post<LoginResponse>("/api/login", payload, { withAuth: false });
    if (data?.token) setToken(data.token as string);
    return data;
  },
  async register(payload: Record<string, unknown>): Promise<unknown> {
    return api.post("/api/register", payload, { withAuth: false });
  },
  async logout(): Promise<void> {
    try { await api.post("/api/logout", null); } catch {}
    clearToken();
  },
};

export default api;
