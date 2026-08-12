# Penpals — Frontend

Vite + React + TypeScript + Tailwind. Talks to the Spring backend at `/api/penpal/**`.

## Run

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev            # http://localhost:5173, proxies /api -> http://localhost:8080
```

Start the backend first (dev profile, port 8080).

## Auth modes (`VITE_AUTH_MODE`)

- `dev` — a login form that sends **HTTP Basic** against the Spring dev profile.
  Users: `penpal` / `parent_helper` / `monitor` / `admin` (password == username).
  For dev only — Basic creds live in `localStorage`; do not use in production.
- `auth0` — real Auth0 login + **bearer JWT**. Set `VITE_AUTH0_DOMAIN`,
  `VITE_AUTH0_CLIENT_ID`, `VITE_AUTH0_AUDIENCE`. Roles are read from the
  `https://penpals.example.com/roles` claim (match your `RealSecurityConfig`).

Both modes go through the same `useAuth()` hook, so pages/components don't care which is active.

## Calling the API

```ts
const api = useApi();

// bearer/Basic header is attached automatically
const users = await api.get<UserFullView[]>('/monitors/all-users');

// acting-as-penpal endpoints: pass the penpal id as the last arg -> X-Acting-As-Penpal
const map = await api.get<PenpalMapRelationshipView>('/penpals/relations', bobId);
await api.post('/penpals/messages', { text: 'hi', chatId: 1 }, bobId);
```

Errors throw `ApiError { status, message }`, where `message` is your
`@RestControllerAdvice` body (`{"message": "..."}`).

## Layout

```
src/
  auth/auth.tsx      unified auth (dev Basic <-> Auth0) behind useAuth()
  api/useApi.ts      fetch wrapper: auth header + X-Acting-As-Penpal + typed helpers
  types.ts           TS mirrors of the backend view DTOs
  pages/             Login, Dashboard (example)
  App.tsx            routing + <Protected> guard
```

Add pages per role (penpal / parent-helper / monitor / admin) using `useApi()` and the
matching endpoints under `/api/penpal/**`.
