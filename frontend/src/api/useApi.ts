import { useAuth } from '../auth/auth';

const BASE = import.meta.env.VITE_API_BASE ?? '/api';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

interface RequestOpts {
  method?: string;
  body?: unknown;
  /** sets the X-Acting-As-Penpal header for the penpal (acting-as) endpoints */
  actingAsPenpal?: number;
}

/**
 * Hook returning typed API helpers. Every call attaches the current auth header
 * (Basic or Bearer) and, if given, the X-Acting-As-Penpal header.
 */
export function useApi() {
  const { getAuthHeader } = useAuth();

  async function request<T>(path: string, opts: RequestOpts = {}): Promise<T> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    const auth = await getAuthHeader();
    if (auth) headers['Authorization'] = auth;
    if (opts.actingAsPenpal != null) headers['X-Acting-As-Penpal'] = String(opts.actingAsPenpal);

    const res = await fetch(`${BASE}${path}`, {
      method: opts.method ?? 'GET',
      headers,
      body: opts.body != null ? JSON.stringify(opts.body) : undefined,
    });

    if (!res.ok) {
      let message = res.statusText;
      try {
        const json = await res.json();
        if (json?.message) message = json.message; // your @RestControllerAdvice shape
      } catch { /* non-JSON body */ }
      throw new ApiError(res.status, message);
    }

    // 201/204 (and any create endpoint) may come back with an empty body — only
    // parse when there's actually something to parse.
    if (res.status === 204) return undefined as T;
    const text = await res.text();
    return (text ? JSON.parse(text) : undefined) as T;
  }

  return {
    get: <T>(path: string, actingAsPenpal?: number) => request<T>(path, { actingAsPenpal }),
    post: <T>(path: string, body: unknown, actingAsPenpal?: number) =>
      request<T>(path, { method: 'POST', body, actingAsPenpal }),
    put: <T>(path: string, body: unknown, actingAsPenpal?: number) =>
      request<T>(path, { method: 'PUT', body, actingAsPenpal }),
    patch: <T>(path: string, body: unknown, actingAsPenpal?: number) =>
      request<T>(path, { method: 'PATCH', body, actingAsPenpal }),
    del: <T>(path: string, actingAsPenpal?: number) =>
      request<T>(path, { method: 'DELETE', actingAsPenpal }),
  };
}
