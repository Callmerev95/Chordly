# Sinkronisasi upstream BitChord v1.10: cherry-pick selektif, package tetap `com.chordly`

## Context

Upstream BitChord merilis `v1.10` (2026-11-21) dengan banyak fitur baru: Replay Year, Google Drive backup, Radio tab, Best Part, YouTube Premium audio, keep-liked-songs-downloaded, animated lyrics share, AutoMix artwork transitions, queue swipe/undo/multi-select, dan puluhan perbaikan lain. Total rentang `v1.9.2..v1.10` mencakup 284 commit, 2.576 file berubah (+33.824/−10.975).

Chordly adalah fork `v1.9.2` upstream (merge-base `cdf42f93`) dengan 49 commit sendiri di atasnya: rebrand penuh ke `com.chordly` / `com.callmerev.chordly`, updater via DownloadManager, party-server sendiri, dan rebrand web/backend. Saat ini Chordly berada di `1.9.4` (versionCode 44).

### Temuan kritis: upstream membalik package

Pada `v1.9.2` upstream sempat memakai package `com.chordly`. Di `v1.10` upstream **membalik ke `com.music.bitchord`**:

- 482 file Java/Kotlin di-rename `com/chordly/*` → `com/music/bitchord/*`.
- 66 file baru semuanya berada di `com/music/bitchord/` (termasuk seluruh `data/backup/*`, `ui/replay/year/*`, `AutoMixVisualState` di `shared/`).
- `app/build.gradle.kts` upstream: `namespace = "com.music.bitchord"`, `applicationId = "com.music.bitchord"`, `bitchordVersionCode = 42`.
- `tv/build.gradle.kts` upstream: `applicationId = "com.music.bitchord.tv"`, `versionCode 42 / versionName "1.10"`, plus `play-services-auth` dan `media3-transformer/effect`.

Artinya merge penuh atau rebase dari upstream akan menyeret 482 rename yang bertentangan dengan identitas Chordly, lalu memaksa re-rebrand masif.

## Decision

1. **Strategi: cherry-pick selektif per fitur.** Ambil commit upstream per tema (lihat mapping di issue #43), di-rebase manual ke struktur package `com.chordly` yang sudah ada. Bukan full merge, bukan rebase penuh.
2. **Package tetap `com.chordly`** untuk namespace kode dan `com.callmerev.chordly` untuk applicationId. Jangan mengikuti balikan upstream ke `com.music.bitchord`.
3. **Cakupan: ambil semua fitur headline dan perbaikan changelog v1.10** (Replay Year, Drive Backup, Radio, Best Part, Premium audio 256k, keep-liked-downloaded, animated lyrics share, AutoMix art transition, queue swipe/undo/multi-select, dan sisanya sesuai daftar ticket).
4. **Version bump di akhir.** Semua ticket fitur dikerjakan lebih dulu di branch `sync/bit-chord-v1.10`; versi dinaikkan ke `1.9.5` (versionCode 45) hanya setelah verifikasi penuh lulus.
5. **Model AutoMix on-demand** tidak lagi di-bundle di APK; diunduh saat pertama kali dipakai. Sumber unduhan diarahkan ke release asset di repo `Callmerev95/Chordly-release` (bukan ke release BitChord).
6. **Drive Backup** memakai scope OAuth minimum `drive.appdata` (folder privat aplikasi), bukan full Drive scope.

## Konsekuensi

- Riwayat git Chordly tetap bersih: tidak ada commit rename balik yang besar.
- Setiap cherry-pick yang menyentuh file di `com.music.bitchord` harus diubah manual ke `com.chordly` saat diterapkan. Ini kerja ekstra per ticket, tapi terkontrol dan bisa diverifikasi per fitur.
- Ticket dikelola di GitHub Issues `Callmerev95/Chordly` (#43 sebagai parent, #44–#63 sebagai T0–T19), label `sync-upstream` + `ready-for-agent`.
- Branch kerja: `sync/bit-chord-v1.10`, dibuat dari `rebrand/chordly`.

## Alternatif yang ditolak

- **Full merge `upstream/main`**: merge-base Chordly masih di `v1.8.2`, jadi merge penuh akan menyeret v1.9 + v1.9.1 + v1.9.2 + v1.10 sekaligus, termasuk 482 rename package. Terlalu berisiko.
- **Rebase penuh `rebrand/chordly` di atas `v1.10`**: 49 commit Chordly harus di-resolve satu per satu di atas 284 commit upstream. Paling mahal.
- **Mengikuti rename upstream ke `com.music.bitchord`**: bertentangan dengan keputusan rebrand (lihat ADR-0001).

## Verification

- Tiap ticket fitur menyatakan sendiri kriteria verifikasinya (build + unit test + perilaku).
- Verifikasi menyeluruh ada di ticket T18 (#62); release di T19 (#63).
- Saat selesai, `grep -r "com.music.bitchord" app/ shared/ sharedUi/ tv/ wearos/` tidak boleh menemukan package aplikasi (kecuali komentar sejarah).
