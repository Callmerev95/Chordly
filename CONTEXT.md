# Chordly

Klien YouTube Music Android untuk penggunaan pribadi. Fork dari BitChord (GPL-3.0-or-later), di-rebrand dan dikembangkan sendiri.

## Bahasa

**BitChord**:
Proyek upstream asal, dipakai sebagai sumber kode.
_Avoid_: bitchord (kecuali sebagai bagian dari identifier teknis yang sengaja dipertahankan)

**Chordly**:
Nama produk fork ini, dipakai di launcher, notifikasi, dan UI.
_Avoid_: BitChord saat merujuk build kita sendiri

**ApplicationId**:
Identitas paket yang dipakai Android untuk memasang dan membedakan aplikasi. Menentukan apakah dua build bisa berdampingan.
_Avoid_: package name (itu istilah Kotlin/Java, bukan identitas pemasangan)

**Namespace**:
Nama paket kode sumber Kotlin/Java (`com.chordly`), dipakai untuk import dan nama class. Tidak harus sama dengan applicationId.
_Avoid_: package id

**Media scheme**:
Skema URI internal aplikasi (`chordly://watch`, `chordly://source`, `chordly://cache`) yang menandai item antrean belum diresolusi. Dibuat dan dibaca hanya di dalam aplikasi.
_Avoid_: deep link (itu untuk tautan dari luar)

**Party deep link**:
Skema `bitchord://party/<kode>` yang dipancarkan server listen-together untuk mengundang bergabung. Sengaja dipertahankan meski produk bernama Chordly, karena server tidak di-rebrand.
_Avoid_: invite scheme Chordly

**Wear capability**:
Nama kemampuan Data Layer (`chordly_phone`, `chordly_watch`) yang dipakai aplikasi jam dan ponsel untuk saling menemukan. Harus identik di kedua sisi.
_Avoid_: wear service

**Native library**:
Kumpulan pustaka C++ yang dibangun CMake dan dimuat lewat `loadLibrary`: analysis, loopmix, dsd, dst, exclusive, usb.
_Avoid_: JNI library (JNI hanya jembatannya)

**Party**:
Sesi mendengarkan bersama yang disinkronkan lewat server Go.
_Avoid_: room, group session