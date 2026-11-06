CHORDLY - ANDROID ADAPTIVE ICON (25 VARIAN)
==========================================
Tiap folder (01_... sampai 25_...) adalah satu paket adaptive icon lengkap:
  res/                      -> salin isinya ke app/src/main/res/ di Android Studio
    mipmap-mdpi..xxxhdpi/   -> ic_launcher_background / _foreground / _monochrome + ic_launcher & ic_launcher_round (legacy)
    mipmap-anydpi-v26/      -> ic_launcher.xml & ic_launcher_round.xml
  Master_1024/              -> layer master 1024 px (108dp) untuk diedit
  PlayStore_Icon_512.png    -> ikon 512x512 untuk Google Play

Cara pakai: pilih SATU folder varian, salin isi res/-nya ke project (timpa file lama).
AndroidManifest: android:icon="@mipmap/ic_launcher"  android:roundIcon="@mipmap/ic_launcher_round"
Layer monochrome dipakai Android 13+ (Themed Icons).
