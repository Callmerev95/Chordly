# AGENTS.md — Chordly

Panduan untuk agent (dan manusia) yang bekerja di repo ini.

Chordly adalah klien YouTube Music Android (fork pribadi dari BitChord, GPL-3.0-or-later).
Dokumen ini menjelaskan konvensi kerja yang dipakai skill engineering di repo ini.

## Agent skills

### Issue tracker

Issue dan spec hidup sebagai GitHub Issues di `Callmerev95/Chordly`. Lihat `docs/agents/issue-tracker.md`.

### Triage labels

Lima peran triage kanonik: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. Lihat `docs/agents/triage-labels.md`.

### Domain docs

Single-context: satu `CONTEXT.md` di root + `docs/adr/` untuk keputusan arsitektur. Lihat `docs/agents/domain.md`.

## Konteks proyek

- **Bahasa dokumen**: Indonesia. Identifier kode, nama file, dan API tetap Inggris.
- **Scope aktif**: klien Android (`app/`, `tv/`, `wearos/`, `shared/`, `sharedUi/`, `native/`).
- **Di luar scope**: `web/`, `backend/`, dan rebranding server listen-together.

## Konvensi kerja

- Satu sesi = satu ticket (kecuali ticket research).
- Tutup sesi dengan skill `handoff` agar sesi berikutnya nyambung.
- Jangan commit tanpa instruksi eksplisit.
- Verifikasi sebelum menyatakan selesai: jalankan build/test yang relevan dan tunjukkan outputnya.
