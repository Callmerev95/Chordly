### ✅ Chordly 1.9 — First Private Release
*Fork of BitChord — Rebranded, Verified, Ready for Personal Use*

#### 🚀 What’s New
- ✅ Full rebranding: `BitChord` → `Chordly` across UI, manifest, resources, and native libraries
- ✅ Verified runtime: `packageName = com.callmerev.chordly`, no `UnsatisfiedLinkError`, deep link `bitchord://party/<code>` works
- ✅ Native audio stack: `chordly_dsd`, `chordly_dst`, `chordly_loopmix`, `chordly_analysis`, `chordly_exclusive`, `chordly_usb` fully integrated & loaded
- ✅ Wear OS & TV support: `:wearos` and `:tv` modules build successfully
- ✅ App icon & theme updated: all launcher icons replaced with custom `Chordly_icon`, dark/light themes refined

#### 🔧 Build & Signing
- Built with JDK 21, Android SDK 37.0, Gradle 8.11.1
- Signed with private keystore (`bitchord-release.jks`) — **not Play Store compatible**, intended for sideload only
- R8 minification enabled, debuggable = false

#### 📦 Artifacts  
- `app-prod-universal-release.apk` — recommended for all devices
- `app-prod-arm64-v8a-release.apk` — optimized for modern phones/tablets
- `app-prod-armeabi-v7a-release.apk` — legacy ARM support
- `app-prod-x86_64-release.apk` — for emulators or Chromebooks

#### ⚠️ Important Notes  
- This is a **private fork** — not affiliated with YouTube, Google, or BitChord upstream
- Backend remains `bitchord.kushagrasingh.in` and `bitchord-listen-together.onrender.com` — unchanged for compatibility
- `bitchord://party/<code>` deep links still work
- License: GPL-3.0-or-later (as inherited from BitChord) — source code available in this repo

#### 🙏 Credits  
- Based on [BitChord](https://github.com/kushagrasinghx/BitChord) by Kushagra Singh
- Icons & branding by Callmerev
- All modifications are documented in `docs/adr/0001-rebrand-chordly.md`