# Chordly

### Aesthetic YouTube Music Client (fork privat)

<br/>

[![Latest release](https://img.shields.io/github/v/release/Callmerev95/Chordly?style=for-the-badge&labelColor=0d1117)](https://github.com/Callmerev95/Chordly/releases)
[![License](https://img.shields.io/github/license/Callmerev95/Chordly?style=for-the-badge&labelColor=0d1117)](https://github.com/Callmerev95/Chordly/blob/main/LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Callmerev95/Chordly/total?style=for-the-badge&labelColor=0d1117)](https://github.com/Callmerev95/Chordly/releases)

<br/>

[**Download**](#download) · [**Features**](#features) · [**Build & Develop**](#build-and-develop) · [**License**](#license) · [**Disclaimer**](#disclaimer)

<br/>

<a href="https://fmhy.net/mobile#youtube-music" target="_blank" rel="noopener noreferrer"><img src="https://cdn.jsdelivr.net/gh/JMcrafter26/badges@main/src/assets/available/fmhy/cozy.svg" alt="Featured on FMHY" height="55"/></a>
<a href="https://trendshift.io/repositories/177639?utm_source=repository-badge&amp;utm_medium=badge&amp;utm_campaign=badge-repository-177639" target="_blank" rel="noopener noreferrer"><img src="https://trendshift.io/api/badge/repositories/177639" alt="kushagrasinghx%2FBitChord | Trendshift" width="250" height="55"/></a>

</div>

> [!IMPORTANT]
> Chordly adalah fork privat dari BitChord dan tidak berafiliasi, didukung, atau terhubung dengan YouTube atau Google. Gunakan sesuai kebijakanmu sendiri.

---

<div align="center">

<img src="Banner.png" alt="Chordly banner" width="100%" />

<h1><a id="features"></a>Features</h1>

<ul>
<li><strong>YouTube Music</strong>: Streaming langsung dari YouTube Music, tanpa akun.</li>
<li><strong>Listen Together</strong>: Bergabung dalam sesi mendengarkan bersama via server upstream <code>bitchord.kushagrasingh.in</code>.</li>
<li><strong>Wear OS</strong>: Remote control untuk aplikasi ponsel dari jam tangan.</li>
<li><strong>TV</strong>: Versi Android TV untuk remote experience.</li>
<li><strong>Audio Quality</strong>: Lossless, DSD, Dolby Atmos, Spatial Audio.</li>
<li><strong>Automix</strong>: DJ transition otomatis antar lagu.</li>
<li><strong>Lyrics Sync</strong>: Lirik bergerak sinkron dengan pemutaran.</li>
<li><strong>Equalizer</strong>: Equalizer 7-band + preset + custom tuning.</li>
<li><strong>Offline Playback</strong>: Unduh lagu untuk didengarkan tanpa koneksi.</li>
<li><strong>Widget</strong>: Widget layar utama untuk kontrol cepat.</li>
</ul>

<h1><a id="download"></a>Download</h1>

Tidak tersedia di Play Store. Untuk versi privat, unduh APK dari <a href="https://github.com/Callmerev95/Chordly/releases">Releases</a>.

<h1><a id="build-and-develop"></a>Build & Develop</h1>

<details>
<summary>Klik untuk instruksi build lokal</summary>

### Prasyarat

- JDK 21 (bukan JDK 27)
- Android SDK (platforms;android-37, build-tools;35.0.0, cmake;3.22.1, ndk;27.0.12077973)
- Gradle Wrapper: `gradlew` (terpasang)

### Build

```bash
# Pastikan toolchain sudah terpasang (lihat T4)
./gradlew :app:assembleDevDebug -Pchordly.bundleWear=false
```

Hasil APK ada di `app/build/outputs/apk/dev/debug/`.

### Debug

Gunakan Android Studio untuk debug Kotlin dan native C++.

</details>

<h1><a id="contributing"></a>Contributing</h1>

Kerjaan ini adalah fork privat — tidak menerima kontribusi eksternal.

<h1><a id="support"></a>Support</h1>

Untuk pertanyaan teknis, kunjungi repo upstream:
- <a href="https://github.com/kushagrasinghx/BitChord/discussions">Discussions</a>
- <a href="https://discord.gg/pDdKfrdHY6">Discord</a>

<h1><a id="license"></a>License</h1>

Chordly adalah fork dari BitChord yang dilisensikan di bawah <a href="https://github.com/kushagrasinghx/BitChord/blob/main/LICENSE">GPL v3.0-or-later</a>. Karena distribusi privat, kamu tidak wajib membagikan source code.

<h1><a id="disclaimer"></a>Disclaimer</h1>

Chordly adalah fork privat dari BitChord. Semua hak cipta dan lisensi tetap milik penulis asli (Kushagra Singh). Penggunaan aplikasi ini sepenuhnya menjadi tanggung jawabmu.

---

<div align="center">

<h1><a id="credits"></a>Credits</h1>

<p>Logo dan nama "Chordly" adalah kreasi pribadi. Semua kode dan fitur lainnya adalah turunan dari <a href="https://github.com/kushagrasinghx/BitChord">BitChord</a>, dilisensikan di bawah GPL v3.0-or-later.</p>

</div>