# Domain Docs

Bagaimana skill engineering mengonsumsi dokumentasi domain repo ini saat mengeksplorasi codebase.

## Sebelum mengeksplorasi, baca ini

- **`CONTEXT.md`** di root repo, atau
- **`CONTEXT-MAP.md`** di root repo bila ada: ia menunjuk satu `CONTEXT.md` per konteks. Baca setiap yang relevan dengan topik.
- **`docs/adr/`**: baca ADR yang menyentuh area yang akan kamu kerjakan. Di repo multi-konteks, cek juga `src/<context>/docs/adr/` untuk keputusan berlingkup konteks.

Jika salah satu file ini tidak ada, **lanjut diam-diam**. Jangan tandai ketidakhadirannya; jangan sarankan membuatnya di muka. Skill `/domain-modeling` (dijangkau via `/grill-with-docs` dan `/improve-codebase-architecture`) membuatnya secara lazy saat istilah atau keputusan benar-benar terselesaikan.

## Struktur file

Repo single-context (sebagian besar repo):

````
/
├── CONTEXT.md
├── docs/adr/
│   ├── 0001-....md
│   └── 0002-....md
└── ...
````

Repo multi-context (adanya `CONTEXT-MAP.md` di root):

````
/
├── CONTEXT-MAP.md
├── docs/adr/                          ← keputusan seluruh sistem
└── ...
````

Repo ini **single-context**: satu `CONTEXT.md` + `docs/adr/` di root. Scope kerja saat ini adalah klien Android (modul `app`, `tv`, `wearos`, `shared`, `sharedUi`).

## Pakai kosakata glosarium

Saat outputmu menyebut konsep domain (di judul issue, proposal refactor, hipotesis, nama tes), pakai istilah sebagaimana didefinisikan di `CONTEXT.md`. Jangan melenceng ke sinonim yang secara eksplisit dihindari glosarium.

Jika konsep yang kamu butuhkan belum ada di glosarium, itu sinyal: entah kamu menemukan bahasa yang tidak dipakai proyek (pertimbangkan ulang) atau ada gap nyata (catat untuk `/domain-modeling`).

## Tandai konflik ADR

Jika outputmu bertentangan dengan ADR yang ada, munculkan secara eksplisit alih-alih menimpanya diam-diam:

> _Bertentangan dengan ADR-0007 (...), tapi layak dibuka ulang karena..._
