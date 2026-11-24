package com.chordly.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chordly.data.model.Song
import com.chordly.playback.PlaybackPosition
import com.chordly.ui.components.MenuFrostDark
import com.chordly.ui.components.liveMenuFrost
import com.chordly.ui.haptics.Haptic
import com.chordly.ui.haptics.rememberHaptics
import com.chordly.ui.icons.ChordlyIcons
import org.jetbrains.compose.resources.stringResource
import com.chordly.sharedui.resources.Res
import com.chordly.sharedui.resources.best_part
import com.chordly.sharedui.resources.best_part_description
import dev.chrisbanes.haze.HazeState

/**
 * "Best Part": a glass pill on the artwork that skips to the part of the song
 * people replay most, or failing that to its chorus.
 *
 * Offered only while it is still ahead: once the playhead has reached the best
 * part, by the button or by just listening, it goes for the rest of the track.
 * A track with no best part known (no replay curve, no synced lyrics to find a
 * chorus in) never shows it, rather than guessing.
 *
 * The same material as the ⋯ menu: its frost, blurring the player under it,
 * with a light rim to read as glass over the artwork.
 */
@Composable
internal fun BestPartPill(
    song: Song,
    position: PlaybackPosition,
    /** Blurred under the pill: the player's own haze. Null draws it flat. */
    hazeState: HazeState?,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Whether a drawer or the ⋯ menu is up over the player. Those blur the
     * player live themselves, so the pill goes flat under them rather than
     * paying for a second live blur nobody can see.
     */
    overlayOpen: () -> Boolean = { false },
) {
    val enabled by PlayerSettings.showBestPart.collectAsStateWithLifecycle()
    if (!enabled) return
    val start by produceState<Long?>(null, song.videoId) {
        value = runCatching { PlayerPlatform.host.bestPartMs(song) }.getOrNull()
    }
    // Remembered per track: having been heard is what puts the button away.
    var heard by remember(song.videoId) { mutableStateOf(false) }
    val at = start
    // Read through derivedStateOf, so the pill recomposes as the playhead
    // crosses the best part, not on every tick of it.
    val ahead by remember(at, position) {
        derivedStateOf { at != null && position.positionMs < at - AHEAD_SLACK_MS }
    }
    val reaching by remember(at, position) {
        // Only a playhead inside the best part counts as hearing it. Further on
        // is a reading left over from the last track, or a skip past it, and
        // the button should come back for a seek to before it.
        derivedStateOf { at != null && position.positionMs in (at - AHEAD_SLACK_MS)..(at + HEARD_WINDOW_MS) }
    }
    LaunchedEffect(reaching) { if (reaching) heard = true }

    // Not while Automix is mixing into the next track: what is ahead then is
    // the next song, not this one's best part.
    val mixing by PlayerSettings.smartMixInProgress.collectAsStateWithLifecycle()

    val haptics = rememberHaptics()
    val reduceDynamicBlur by PlayerSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    AnimatedVisibility(
        visible = at != null && ahead && !heard && !mixing,
        // A fade either way. The room it takes opens and closes around the
        // fade, so the stats over it glide instead of jumping.
        enter = expandVertically(tween(BEST_PART_FADE_MS)) +
            fadeIn(tween(BEST_PART_FADE_MS, delayMillis = BEST_PART_FADE_MS / 2)),
        exit = fadeOut(tween(BEST_PART_FADE_MS)) +
            shrinkVertically(tween(BEST_PART_FADE_MS, delayMillis = BEST_PART_FADE_MS / 2)),
        modifier = modifier,
    ) {
        val shape = CircleShape
        val label = stringResource(Res.string.best_part)
        val description = stringResource(Res.string.best_part_description)
        val chrome = LocalPlayerChrome.current
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(shape)
                .glassFrost(hazeState, flat = reduceDynamicBlur || overlayOpen())
                .clickable(role = Role.Button) {
                    val target = at ?: return@clickable
                    haptics.play(Haptic.Tap)
                    heard = true
                    onSeek(target)
                }
                .semantics { contentDescription = description },
        ) {
            Box(Modifier.matchParentSize().glassRim(shape))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 15.dp, top = 7.dp, bottom = 7.dp),
            ) {
                Icon(
                    imageVector = ChordlyIcons.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W700,
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Just short of the best part already counts as there, so the pill doesn't flash back on a late reading. */
private const val AHEAD_SLACK_MS = 400L

/** How far into the best part a playhead still counts as having heard it. */
private const val HEARD_WINDOW_MS = 20_000L

private const val BEST_PART_FADE_MS = 280

/** Thinner than the menu's slab, so the glass reads as glass over the artwork. */
private const val GLASS_SLAB_ALPHA = 0.38f

/** The slab alone, with no blur under it, thick enough to hold the glyph on any cover. */
private const val GLASS_FLAT_ALPHA = 0.72f

/**
 * Glass's body: the ⋯ menu's frost, blurring the player live under a thin
 * charcoal slab. [flat] (dynamic blur reduced, or a drawer or the ⋯ menu up
 * over it, which blur the player live themselves) and a missing [hazeState]
 * draw the slab alone, thicker, and blur nothing.
 */
internal fun Modifier.glassFrost(hazeState: HazeState?, flat: Boolean): Modifier =
    if (flat || hazeState == null) {
        background(MenuFrostDark.copy(alpha = GLASS_FLAT_ALPHA))
    } else {
        liveMenuFrost(hazeState, MenuFrostDark, alpha = GLASS_SLAB_ALPHA)
    }

/**
 * The star and ⋯ discs beside the credits, behind one frost between them.
 *
 * One blur, cut to both circles, rather than one per disc: the two always move
 * together (a skip's slide, the collapse), and each would otherwise be blurred
 * again on every frame of it, side by side, from the same source.
 */
@Composable
internal fun GlassDiscPair(
    hazeState: HazeState?,
    /** See [BestPartPill]'s, which goes flat for the same reason. */
    overlayOpen: () -> Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val reduceDynamicBlur by PlayerSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .clip(DiscPairShape)
                .glassFrost(hazeState, flat = reduceDynamicBlur || overlayOpen()),
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

/**
 * A disc at each end of a row as tall as one: both glyphs, or the ⋯ alone
 * (a row no wider than it is tall) for a guest, who gets no star.
 */
private val DiscPairShape = GenericShape { size, _ ->
    val d = size.height
    addOval(Rect(0f, 0f, d, d))
    if (size.width >= d * 1.5f) addOval(Rect(size.width - d, 0f, size.width, d))
}

/**
 * Glass's edge: a hairline rim, brighter along the top where the light would
 * catch it. One rim for the Best Part pill and the star and ⋯ discs, so they
 * read as the same material.
 */
internal fun Modifier.glassRim(shape: Shape): Modifier = border(
    width = 1.dp,
    brush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.42f),
        1f to Color.White.copy(alpha = 0.14f),
    ),
    shape = shape,
)
