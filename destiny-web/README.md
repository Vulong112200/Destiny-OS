# Destiny OS — Frontend

Next.js (App Router, TypeScript, Tailwind) Decision Center for Destiny OS. Not part of the Maven reactor — a separate `npm` project, calling `destiny-api` over HTTP.

See the root [`README.md`](../README.md) for full project context, and `docs/UI_UX_VIETNAMESE_SPEC.md` for the UI spec this implements.

## Scope

Only three pages are built, each backed by a real endpoint today:

- **Tổng quan** (`/`) — methodology registry status (`GET /api/v1/methodologies`)
- **Trung tâm quyết định** (`/trung-tam-quyet-dinh`) — the Decision Center flow (`POST /api/v1/scenarios/{type}`, `BUSINESS`/`DAILY_ACTION` only)
- **Lịch sử** (`/lich-su`) — look up a calculation by id (`GET /api/v1/calculations/{id}`)

Everything else in the spec's nav is labeled "Sắp ra mắt" rather than built against nothing — see `src/app/layout.tsx`.

## Running

```bash
npm install
cp .env.local.example .env.local
npm run dev
```

Requires `destiny-app` running (see root README) — `NEXT_PUBLIC_API_BASE_URL` in `.env.local` points at it.

## Type contract

`src/lib/types.ts` mirrors `destiny-api`'s DTOs field-for-field by hand — there is no shared schema generator yet, so keep it in sync when a DTO changes.
