package com.chordly.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppUpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != -1L) {
                    AppUpdateChecker.onDownloadCompleted(context, id)
                }
            }
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                val file = AppUpdateChecker.getDownloadedApk(context)
                if (file != null && file.exists()) {
                    AppUpdateChecker.installApk(context, file)
                } else {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    }
                }
            }
        }
    }
}
