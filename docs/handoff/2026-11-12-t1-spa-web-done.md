# Handoff — Chordly web, T1 (SPA fallback) SELESAI

**Tanggal**: 2026-11-12
**Repo**: `/Users/rev/Myself/work/Private_Project/Music_Client/Chordly`
**Branch**: `rebrand/chordly`
**Map**: https://github.com/Callmerev95/Chordly/issues/37
**Ticket**: T1 = https://github.com/Callmerev95/Chordly/issues/38 (closed, completed)
**Tiket berikutnya**: T2 = https://github.com/Callmerev95/Chordly/issues/39 (blocked by T1, unblocked)

## Status sesi ini

- HEAD `7523654c` (commit T1) — `web/vercel.json` SPA rewrite.
- Masalah: halaman client-side (`/extension`, `/home`) balik 404 pas buka langsung / reload karena repo tak punya `vercel.json`.

## Perubahan

`web/vercel.json`:
```json
{ "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }] }
```

## Gate (semua PASS, live domain)

- `https://chordly.callmerev.my.id/` → 200
- `https://chordly.callmerev.my.id/extension` → 200 (sebelumnya 404)
- `https://chordly.callmerev.my.id/home` → 200 (sebelumnya 404)
- `https://chordly.callmerev.my.id/downloads/extension.json` → 200 (statis tetap jalan; Vercel layani file dulu, rewrite terakhir)

## Catatan

- Vercel menyajikan file statis (`/downloads/*`, `/assets/*`) sebelum rewrite, jadi download zip `chordly-extension-*.zip` tak terganggu — terverifikasi 200.
- Tak ada perubahan kode JS; murni konfigurasi hosting.

## Urutan selanjutnya

- T2 (#39): deploy server party self-host ke Render (`chordly-*`) + arahkan `VITE_PARTY_SERVER_URL`. Unblocked oleh T1.
