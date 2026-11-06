# Rebrand ke Chordly: identitas, distribusi privat, updater dimatikan

## Context

Rebrand BitChord menjadi Chordly untuk penggunaan pribadi. Ini bukan fork publik — tidak akan di-distribusikan ke orang lain.

## Decision

- `applicationId` diubah menjadi `com.callmerev.chordly` (prod) dan `com.callmerev.chordly.dev` (dev).
- Semua nama tampilan (`app_name`, UI text, logs) diubah dari `BitChord`/`bitchord` → `Chordly`.
- Logo launcher dibuat ulang dari `Chordly_logo.png`.
- Fitur auto-update (`AppUpdateChecker`) dimatikan karena tidak relevan untuk versi privat dan bisa menyebabkan install gagal jika mengarah ke APK upstream.
- Backend server (`bitchord.kushagrasingh.in`, `bitchord-listen-together.onrender.com`) dan deep link `bitchord://party` tetap utuh — tidak di-rebrand — agar integrasi dengan server upstream tetap berfungsi.
- Lisensi tetap GPL v3.0-or-later. Karena distribusi privat, tidak ada kewajiban membagikan source code.

## Status

accepted

## Verification

- `:app` prod build sukses dan bundel Wear OS terpaket (T6)
- `:tv` devDebug build sukses (T7)
- `:wearos` devDebug build sukses (T7)
- Semua native library (`chordly_analysis`, `chordly_loopmix`, `chordly_dsd`, `chordly_dst`, `chordly_exclusive`, `chordly_usb`) diverifikasi termuat di APK dan dimuat saat runtime (T6)