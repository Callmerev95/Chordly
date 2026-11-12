# Deploy web Chordly ke Vercel (dashboard)

Panduan mengulang deployment web player Chordly (fork BitChord) ke Vercel,
semua via dashboard. Repo: `Callmerev95/Chordly`.

## Prasyarat

- Default branch GitHub = `rebrand/chordly`. Semua kerja rebrand (T17–T24)
  sudah di sana dan ter-push → Vercel build dari branch ini.
- Setting build sudah terdokumentasi di `web/README.md` (Root/branch
  `web`, build `pnpm build:site`, output `app/dist`).

## Environment variables (wajib)

| Variable | Nilai | Keterangan |
| --- | --- | --- |
| `NODE_VERSION` | `22` | `web/package.json` `engines.node: >=22` |
| `VITE_PARTY_SERVER_URL` | `https://api.bitchord.kushagrasingh.in` | Server Listen Together **upstream** (ADR 0001, tetap tak di-rebrand). `vite build` **gagal** tanpa nilai ini (`web/app/vite.config.ts`) |

Jangan pakai `https://party.invalid` — itu placeholder lokal
(`web/app/.env.local`) biar build dev lolos, bukan nilai produksi.

Asal nilai server upstream: `backend/README.md` (*"This is what runs
`https://api.bitchord.kushagrasingh.in`"*), `docs/web-app-build-prompt.md`,
`shared/.../data/presence/Presence.kt`. Alternatif upstream kedua:
`bitchord-listen-together.onrender.com` (disebut di ADR 0001).

## Langkah (dashboard)

1. **Import proyek**: `vercel.com` → login akun GitHub pemilik repo →
   **Add New… → Project** → pilih `Callmerev95/Chordly` → **Import**.

2. **Config build** (Project Settings / Configure):

   | Setting | Nilai |
   | --- | --- |
   | Root Directory | `web` |
   | Framework Preset | `Vite` |
   | Build Command | `pnpm build:site` |
   | Output Directory | `app/dist` |
   | Install Command | (kosongkan — Vercel pakai `pnpm` dari `packageManager` di `web/package.json`) |

3. **Environment variables**: tambah `NODE_VERSION=22` dan
   `VITE_PARTY_SERVER_URL=https://api.bitchord.kushagrasingh.in`.

4. **Deploy**, tunggu build selesai.

   Jika gagal, penyebab umum:
   - `VITE_PARTY_SERVER_URL` kelewat → `vite build` throw (cari di build log).
   - Node <22 → pastikan `NODE_VERSION=22`.

5. **Cek preview** di URL deployment:
   - `GET /downloads/extension.json` → `files[]` = `chordly-extension-*`.
   - `GET /downloads/chordly-extension-chrome%20(0.4.4).zip` → 200
     (nama zip di-URL-encode karena ada spasi + bracket).

6. **Domain custom** (kalau belum): Project → **Settings → Domains** →
   **Add** → `chordly.callmerev.my.id` → ikuti instruksi DNS Vercel
   (CNAME/ALIAS ke `cname.vercel-dns.com`, atau nameserver Vercel).
   **Production Branch** = `rebrand/chordly`.

7. **Verifikasi final** di `https://chordly.callmerev.my.id`:
   - Halaman `/extension` render + unduh zip bekerja.
   - Pasang unpacked dari `chordly-extension-{chrome,firefox}.zip` →
     status `Installed · v0.4.4`.
   - Listen Together: buat party → terhubung (butuh server upstream hidup).

## Catatan

- `build:site` mem-package extension dulu, jadi tiap deploy mem-publish zip
  `chordly-extension-*` + `downloads/extension.json` versi terbaru
  (dari `web/extension/package.json` version). Bump version itu untuk
  menawarkan update.
- Kontrak nama `chordly-extension-` (3 titik sinkron; ubah satu = download
  404): `extension/scripts/publish-to-site.mjs` ↔ `app/src/config.ts` ↔
  `app/src/pages/ExtensionPage.tsx`.
- Env override `VITE_EXTENSION_CHROME_URL` / `VITE_EXTENSION_FIREFOX_URL`
  bila dipakai di Vercel harus ikut nama `chordly-extension-*`.
- Jika server upstream Listen Together mati, fitur party tak connect — di
  luar scope proyek (bukan bagian rebrand).
