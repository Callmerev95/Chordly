package com.chordly.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ACTION_QUEUE_REMOVE_WITH_UNDO = "com.chordly.action.QUEUE_REMOVE_WITH_UNDO"
const val ACTION_QUEUE_REMOVE_ENTRIES_WITH_UNDO = "com.chordly.action.QUEUE_REMOVE_ENTRIES_WITH_UNDO"
const val ACTION_QUEUE_CLEAR_WITH_UNDO = "com.chordly.action.QUEUE_CLEAR_WITH_UNDO"
const val ACTION_QUEUE_UNDO = "com.chordly.action.QUEUE_UNDO"
const val EXTRA_QUEUE_UNDO_TOKEN = "chordly.queueUndo.token"
const val EXTRA_QUEUE_BULK_MEDIA_IDS = "chordly.queueBulk.mediaIds"
const val EXTRA_QUEUE_BULK_ENTRY_IDS = "chordly.queueBulk.entryIds"
const val EXTRA_QUEUE_BULK_ENTRY_ID_PRESENT = "chordly.queueBulk.entryIdPresent"
const val EXTRA_QUEUE_BULK_OCCURRENCES = "chordly.queueBulk.occurrences"

/** Process-local, latest-only UI feedback; never persisted or sent to a party. */
data class QueueUndoNotice(
    val token: String,
    val message: String,
    val removedCount: Int? = null,
    val skippedCount: Int = 0,
)

object QueueScreenUndo {
    private val mutableNotice = MutableStateFlow<QueueUndoNotice?>(null)
    val notice: StateFlow<QueueUndoNotice?> = mutableNotice.asStateFlow()

    internal fun publish(notice: QueueUndoNotice) {
        mutableNotice.value = notice
    }

    internal fun clear() {
        mutableNotice.value = null
    }
}
