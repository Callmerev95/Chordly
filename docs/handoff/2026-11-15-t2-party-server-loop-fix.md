# Handoff — T2 party server & loop bug fix

**Tanggal**: 2026-11-15
**Branch**: `rebrand/chordly`
**Ticket**: T2 (#39) — deploy server party self-host + arahkan `VITE_PARTY_SERVER_URL`

## Status

T2 SELESAI. Loop bug (Issue #41) juga sudah di-fix di sesi ini.

## Yang dikerjakan

### 1. Server party self-host (T2)

- `backend/render.yaml` — rename service `bitchord-listen-together` → `chordly-listen-together`, tambah `JAM_ALLOWED_ORIGINS`.
- `backend/main.go` — `handleRoot` service string → `chordly-listen-together`.
- Deploy ke **TheVibeHosting** (bukan Render — Render minta card). Server live: `https://chordly-party.thevibehosting.com`.
- Invite URL sekarang: `https://chordly-party.thevibehosting.com/invite/<CODE>`.

### 2. Build & deploy pipeline

- `.github/workflows/build-party-server.yml` — build binary linux/amd64 via GH Actions, upload artifact `chordly-party-server`.
- Deploy manual: download artifact → scp ke VPS → restart. Kredensial di `.vibe/` (gitignored).

### 3. Rebrand invite page

- Title, heading, subtitle, button → "Chordly".
- Download button → `Chordly-release/releases/download/v1.9.4/app-prod-universal-release.apk`.
- Desain mengikuti BitChord: dot-matrix + grid-lines background, logo header, music icon, copy-code button, install pill button.
- Favicon + Open Graph meta tags (`og:title`, `og:description`, `og:image` = `Chordly_logo.png`).
- Static file handler ditambah di `main.go` untuk serve `/favicon.svg` dan `/Chordly_logo.png` dari `/root/www/`.

### 4. `INVITE_ORIGIN` fix

- `web/app/src/party/invite.ts` — hardcoded `INVITE_ORIGIN` diubah ke `https://chordly-party.thevibehosting.com`. Ini penyebab invite link masih menunjuk `bitchord.kushagrasingh.in`.

### 5. Loop bug fix (Issue #41)

**Gejala**: lagu restart mundur saat Listen Together, konsisten di 2:59-3:20. Solo listening tidak kena.

**Akar masalah (3 lapis, ditemukan via logging):**

1. **`track.durationMs` dari YouTube API lebih pendek dari video aktual.** `partyPositionMs()` cap posisi di `track.durationMs`. Player maju melewati durasi reported → server cap posisi → drift 10+ detik → seek mundur → looping. **Fix utama**: `partyPositionMs()` sekarang pakai `getAudio()?.duration` (durasi aktual audio element), bukan `track.durationMs`. Commit `8a5dda84`.
2. **Extension broadcast `positionMs: 0` saat YouTube auto-loop.** Host-side guard di `publish()` + client-side guard di `onFrame` state handler skip frame/seek saat position drop drastis + track sama + isPlaying sama. Commit `e1c6c7f5`.
3. **Rapid seekTo overwhelm audio element** → `AbortError` / "No YouTube client". Fix: try-catch + reload track pada error, cooldown naik ke 10s. Commit `764f2658`.

Debug logging sudah dibersihkan. Commit `03a01722`.

## Keputusan & konvensi baru

- **Server party = `chordly-listen-together`** di TheVibeHosting, bukan Render. `VITE_PARTY_SERVER_URL` contract berubah ke server sendiri.
- **`INVITE_ORIGIN`** di `invite.ts` adalah hardcoded constant — harus diupdate manual kalau ganti server.
- **`track.durationMs` dari API tidak reliable** untuk cap posisi — pakai `audio.duration` dari elemen audio.

## Out of scope / belum dikerjakan

- **T3 (#40)**: ubah deep link scheme `bitchord://` → `chordly://`. Butuh rebuild Android APK. Ticket terpisah.
- **Issue #41**: ticket loop bug — sudah di-fix di sesi ini, bisa ditutup.
- **Bug autoplay saat Listen Together**: autoplay jalan normal solo, tapi tidak jalan saat party. Terakhir diinvestigasi: `refillAutoplay()` di `player.ts:795` skip saat `partyHooks?.inParty()`, dan ada sistem party-autoplay terpisah di `partySync.ts:417-464` ("one supplier for the whole party" via `autoplaySupplierId`). Belum ditemukan akar masalahnya. **Ini tiket berikutnya.**

## Langkah selanjutnya

1. Investigasi & fix bug autoplay saat Listen Together (lihat `partySync.ts:417-464`, `queueReading.ts:autoplaySupplierId`, `player.ts:795,856`).
2. Tutup Issue #41 (loop bug sudah fixed).
3. T3 (#40): Android scheme update — butuh rebuild APK.
