# Issue tracker: GitHub

Issue dan spec untuk repo ini hidup sebagai GitHub Issues di `Callmerev95/Chordly`.
Gunakan `gh` CLI untuk semua operasi.

## Konvensi

- **Buat issue**: `gh issue create --title "..." --body "..."`. Gunakan heredoc untuk body multi-baris.
- **Baca issue**: `gh issue view <number> --comments`, filter komentar dengan `jq` dan ambil juga label.
- **Daftar issue**: `gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'` dengan filter `--label` dan `--state` sesuai kebutuhan.
- **Komentar**: `gh issue comment <number> --body "..."`
- **Tambah / hapus label**: `gh issue edit <number> --add-label "..."` / `--remove-label "..."`
- **Close**: `gh issue close <number> --comment "..."`

Repo di-infer dari `git remote -v`; `gh` melakukannya otomatis saat dijalankan di dalam clone.

## Pull request sebagai permukaan triage

**PRs as a request surface: no.** _(Set ke `yes` bila repo ini memperlakukan PR eksternal sebagai permintaan fitur; `/triage` membaca flag ini.)_

Saat di-set `yes`, PR melewati label dan state yang sama dengan issue, memakai padanan `gh pr`:
- **Baca PR**: `gh pr view <number> --comments` dan `gh pr diff <number>` untuk diff-nya.
- **Daftar PR eksternal untuk triage**: `gh pr list --state open --json number,title,body,labels,author,authorAssociation,comments` lalu simpan hanya `authorAssociation` bernilai `CONTRIBUTOR`, `FIRST_TIME_CONTRIBUTOR`, atau `NONE` (buang `OWNER`/`MEMBER`/`COLLABORATOR`).
- **Komentar / label / close**: `gh pr comment`, `gh pr edit --add-label`/`--remove-label`, `gh pr close`.

GitHub memakai satu ruang nomor untuk issue dan PR, jadi `#42` bisa salah satu: cek dengan `gh pr view 42`, jika gagal fallback ke `gh issue view 42`.

## Saat sebuah skill berkata "publish ke issue tracker"

Buat GitHub issue.

## Saat sebuah skill berkata "fetch ticket yang relevan"

Jalankan `gh issue view <number> --comments`.

## Operasi wayfinding

Digunakan oleh `/wayfinder`. **Map** adalah satu issue dengan **child** issue sebagai ticket.

- **Map**: satu issue berlabel `wayfinder:map`, memuat body Notes / Decisions-so-far / Fog. `gh issue create --label wayfinder:map`.
- **Child ticket**: issue yang ditautkan ke map sebagai GitHub sub-issue (`gh api` ke endpoint sub-issues). Bila sub-issues tidak aktif, tambahkan child ke task list di body map dan tulis `Part of #<map>` di baris atas body child. Label: `wayfinder:<type>` (`research`/`prototype`/`grilling`/`task`). Setelah di-claim, ticket di-assign ke dev yang mengerjakan.
- **Blocking**: **native issue dependencies** GitHub, representasi kanonik yang terlihat di UI. Tambah edge dengan `gh api --method POST repos/<owner>/<repo>/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`, di mana `<blocker-db-id>` adalah **database id** numerik blocker (`gh api repos/<owner>/<repo>/issues/<n> --jq .id`, **bukan** `#number` atau `node_id`). GitHub melaporkan `issue_dependencies_summary.blocked_by` (hanya blocker terbuka, gate hidup). Bila dependencies tidak tersedia, fallback ke baris `Blocked by: #<n>, #<n>` di baris atas body child. Ticket unblocked ketika semua blocker closed.
- **Frontier query**: daftar child terbuka milik map (`gh issue list --state open`, dibatasi sub-issue / task list map), buang yang punya blocker terbuka (`issue_dependencies_summary.blocked_by > 0`, atau issue terbuka di baris `Blocked by`) atau punya assignee; yang pertama dalam urutan map menang.
- **Claim**: `gh issue edit <n> --add-assignee @me`, write pertama sesi.
- **Resolve**: `gh issue comment <n> --body "<answer>"`, lalu `gh issue close <n>`, lalu append context pointer (gist + link) ke Decisions-so-far map.
