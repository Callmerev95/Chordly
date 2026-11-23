package com.chordly.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ACTION_QUEUE_REMOVE_WITH_UNDO = "com.chordly.action.QUEUE_REMOVE_WITH_UNDO"
const val ACTION_QUEUE_CLEAR_WITH_UNDO = "com.chordly.action.QUEUE_CLEAR_WITH_UNDO"
const val ACTION_QUEUE_UNDO = "com.chordly.action.QUEUE_UNDO"
const val EXTRA_QUEUE_UNDO_TOKEN = "chordly.queueUndo.token"

/** Process-local, latest-only UI feedback; never persisted or sent to a party. */
data class QueueUndoNotice(val token: String, val message: String)

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
