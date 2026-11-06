package com.chordly

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * The home-screen icon choices. Each is an `<activity-alias>` in the manifest
 * carrying the launcher intent filter; exactly one is enabled at a time.
 *
 * Every icon is an adaptive icon whose foreground already holds the finished
 * icon in its own shape, so [previewRes] shows the variant as designed rather
 * than through the device's launcher mask. The first entry keeps the alias name
 * `LauncherDefault` so a choice made before the update stays enabled.
 */
enum class AppIcon(val alias: String, val iconRes: Int, val labelRes: Int, val previewRes: Int? = null) {
    OriginalHotPink("LauncherDefault", R.mipmap.ic_chordly_01, R.string.app_icon_original_hot_pink, R.mipmap.ic_chordly_01_foreground),
    OceanBlue("LauncherChordly02", R.mipmap.ic_chordly_02, R.string.app_icon_ocean_blue, R.mipmap.ic_chordly_02_foreground),
    EmeraldGreen("LauncherChordly03", R.mipmap.ic_chordly_03, R.string.app_icon_emerald_green, R.mipmap.ic_chordly_03_foreground),
    SunsetOrange("LauncherChordly04", R.mipmap.ic_chordly_04, R.string.app_icon_sunset_orange, R.mipmap.ic_chordly_04_foreground),
    RoyalPurple("LauncherChordly05", R.mipmap.ic_chordly_05, R.string.app_icon_royal_purple, R.mipmap.ic_chordly_05_foreground),
    AquaTeal("LauncherChordly06", R.mipmap.ic_chordly_06, R.string.app_icon_aqua_teal, R.mipmap.ic_chordly_06_foreground),
    GoldenYellow("LauncherChordly07", R.mipmap.ic_chordly_07, R.string.app_icon_golden_yellow, R.mipmap.ic_chordly_07_foreground),
    DarkGlass("LauncherChordly08", R.mipmap.ic_chordly_08, R.string.app_icon_dark_glass, R.mipmap.ic_chordly_08_foreground),
    FrostedWhite("LauncherChordly09", R.mipmap.ic_chordly_09, R.string.app_icon_frosted_white, R.mipmap.ic_chordly_09_foreground),
    LiquidGlass("LauncherChordly10", R.mipmap.ic_chordly_10, R.string.app_icon_liquid_glass, R.mipmap.ic_chordly_10_foreground),
    NeonGlow("LauncherChordly11", R.mipmap.ic_chordly_11, R.string.app_icon_neon_glow, R.mipmap.ic_chordly_11_foreground),
    FlatMinimal("LauncherChordly12", R.mipmap.ic_chordly_12, R.string.app_icon_flat_minimal, R.mipmap.ic_chordly_12_foreground),
    SunsetGradient("LauncherChordly13", R.mipmap.ic_chordly_13, R.string.app_icon_sunset_gradient, R.mipmap.ic_chordly_13_foreground),
    AuroraMesh("LauncherChordly14", R.mipmap.ic_chordly_14, R.string.app_icon_aurora_mesh, R.mipmap.ic_chordly_14_foreground),
    StickerLight("LauncherChordly15", R.mipmap.ic_chordly_15, R.string.app_icon_sticker_light, R.mipmap.ic_chordly_15_foreground),
    CircleRadial("LauncherChordly16", R.mipmap.ic_chordly_16, R.string.app_icon_circle_radial, R.mipmap.ic_chordly_16_foreground),
    DarkAppTile("LauncherChordly17", R.mipmap.ic_chordly_17, R.string.app_icon_dark_app_tile, R.mipmap.ic_chordly_17_foreground),
    SoftPastel("LauncherChordly18", R.mipmap.ic_chordly_18, R.string.app_icon_soft_pastel, R.mipmap.ic_chordly_18_foreground),
    Monochrome("LauncherChordly19", R.mipmap.ic_chordly_19, R.string.app_icon_monochrome, R.mipmap.ic_chordly_19_foreground),
    OutlineLine("LauncherChordly20", R.mipmap.ic_chordly_20, R.string.app_icon_outline_line, R.mipmap.ic_chordly_20_foreground),
    EmbossPink("LauncherChordly21", R.mipmap.ic_chordly_21, R.string.app_icon_emboss_pink, R.mipmap.ic_chordly_21_foreground),
    EmbossIvory("LauncherChordly22", R.mipmap.ic_chordly_22, R.string.app_icon_emboss_ivory, R.mipmap.ic_chordly_22_foreground),
    RaisedDarkNote("LauncherChordly23", R.mipmap.ic_chordly_23, R.string.app_icon_raised_dark_note, R.mipmap.ic_chordly_23_foreground),
    DebossDark("LauncherChordly24", R.mipmap.ic_chordly_24, R.string.app_icon_deboss_dark, R.mipmap.ic_chordly_24_foreground),
    DarkMode("LauncherChordly25", R.mipmap.ic_chordly_25, R.string.app_icon_dark_mode, R.mipmap.ic_chordly_25_foreground);

    private fun component(context: Context) =
        ComponentName(context.packageName, "com.chordly.$alias")

    companion object {
        fun current(context: Context): AppIcon {
            val pm = context.packageManager
            return entries.firstOrNull {
                pm.getComponentEnabledSetting(it.component(context)) ==
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } ?: OriginalHotPink
        }

        /** Enables [icon] before disabling the rest so there is never a moment with no launcher entry. */
        fun apply(context: Context, icon: AppIcon) {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                icon.component(context),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            entries.filter { it != icon }.forEach {
                pm.setComponentEnabledSetting(
                    it.component(context),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
