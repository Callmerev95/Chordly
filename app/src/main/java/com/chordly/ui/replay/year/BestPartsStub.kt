package com.chordly.ui.replay.year

import android.content.Context
import com.chordly.data.model.Song
import com.chordly.data.model.durationMillis

/**
 * ponytail: placeholder for the best-part lookup that T15 (#59) ports in full
 * (upstream BestPart.kt + BestParts.kt). Every song takes its fallback start,
 * which is exactly what upstream does when neither the "Most replayed" curve
 * nor synced lyrics answered. Swap this file for the real implementation in
 * T15; nothing outside ReplayYearStories calls into it.
 *
 * Where a song's best part starts, and what said so.
 */
data class BestPartStart(val startMs: Long, val source: Source) {
    enum class Source {
        /** The bump in YouTube's "Most replayed" curve. */
        HEATMAP,

        /** The first time the chorus comes round in the synced lyrics. */
        LYRICS,

        /** Neither had anything to say: a fixed point a way into the song. */
        FALLBACK,
    }
}

/** Somewhere past the intro for a song nothing else could place. */
object BestPart {
    fun fallbackMs(durationMs: Long): Long = when {
        durationMs <= 0 -> FALLBACK_UNKNOWN_MS
        durationMs < MIN_SONG_MS -> 0L
        else -> maxOf(FALLBACK_MIN_MS, (durationMs * FALLBACK_SHARE).toLong())
            .coerceAtMost(durationMs - SKIP_TAIL_MS)
    }

    private const val FALLBACK_UNKNOWN_MS = 45_000L
    private const val FALLBACK_MIN_MS = 60_000L
    private const val FALLBACK_SHARE = 0.55f
    private const val SKIP_TAIL_MS = 30_000L
    private const val MIN_SONG_MS = 90_000L
}

/** Songs' best parts for the Replay year story. Placeholder — see above. */
object BestParts {

    private val YOUTUBE_ID = Regex("[A-Za-z0-9_-]{11}")

    /** Whether [id] is a YouTube video id rather than a file or an addon's key. */
    fun isYouTubeId(id: String): Boolean = YOUTUBE_ID.matches(id)

    /** What is already known about [videoId], without asking anything. */
    fun known(videoId: String): BestPartStart? = null

    /**
     * Where [song]'s best part starts. Always the fallback until T15 lands the
     * real lookup.
     */
    suspend fun startFor(context: Context, song: Song): BestPartStart =
        BestPartStart(BestPart.fallbackMs(song.durationMillis()), BestPartStart.Source.FALLBACK)
}
