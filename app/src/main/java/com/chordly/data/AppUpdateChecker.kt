package com.chordly.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.chordly.BuildConfig
import com.chordly.data.DebugLog as Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import java.io.File

object AppUpdateChecker {

    data class UpdateInfo(
        val version: String,
        val releaseUrl: String,
        val apkUrl: String?,
        val notes: String?,
    )

    private const val CACHE_SUBDIR = "updates"
    private const val TAG = "AppUpdate"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_DOWNLOAD_ID = "update_download_id"
    private const val KEY_DOWNLOAD_VERSION = "update_download_version"

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/Callmerev95/Chordly-release/releases/latest"

    private val json = Json { ignoreUnknownKeys = true }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    private val _available = MutableStateFlow<UpdateInfo?>(null)
    val available = _available.asStateFlow()

    sealed interface DownloadState {
        data object Idle : DownloadState
        data class Downloading(val fraction: Float) : DownloadState
        data class Ready(val file: File) : DownloadState
        data class Failed(val message: String) : DownloadState
    }

    private val _download = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val download = _download.asStateFlow()

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApkFile(context: Context, version: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "Download").apply { mkdirs() }
        return File(dir, "chordly-$version.apk")
    }

    fun getDownloadedApk(context: Context): File? {
        val version = _available.value?.version
            ?: getPrefs(context).getString(KEY_DOWNLOAD_VERSION, null)
            ?: return null
        val file = getApkFile(context, version)
        return if (file.exists() && file.length() > 0) file else null
    }

    suspend fun check() = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(LATEST_RELEASE_URL)
                .header("Accept", "application/vnd.github+json")
                .build()
            val body = Http.client.newCall(request).execute().use { response ->
                Log.d(TAG, "release API HTTP ${response.code}")
                if (!response.isSuccessful) null else response.body?.string()
            } ?: return@runCatching
            val release = json.parseToJsonElement(body) as? JsonObject ?: return@runCatching
            val tag = release["tag_name"]?.jsonPrimitive?.contentOrNull ?: return@runCatching
            val url = release["html_url"]?.jsonPrimitive?.contentOrNull ?: return@runCatching
            val apkUrl = apkAssetUrl(release)
            val notes = release["body"]?.jsonPrimitive?.contentOrNull
            val latest = tag.removePrefix("v")
            val newer = isNewer(latest, BuildConfig.VERSION_NAME)
            Log.d(
                TAG,
                "tag=$tag latest=$latest current=${BuildConfig.VERSION_NAME} " +
                    "isNewer=$newer apkUrl=${apkUrl != null}",
            )
            if (newer) {
                _available.value = UpdateInfo(latest, url, apkUrl, notes)
            }
        }.onFailure { error ->
            Log.w(TAG, "update check failed", error)
        }
    }

    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        File(context.cacheDir, CACHE_SUBDIR).listFiles()?.forEach { it.delete() }
        val prefs = getPrefs(context)
        val savedVersion = prefs.getString(KEY_DOWNLOAD_VERSION, null)
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir?.listFiles()?.forEach { file ->
            if (file.name.startsWith("chordly-", ignoreCase = true) && file.name.endsWith(".apk", ignoreCase = true)) {
                if (savedVersion == null || file.name != "chordly-$savedVersion.apk") {
                    file.delete()
                }
            }
        }
    }

    private fun apkAssetUrl(release: JsonObject): String? = runCatching {
        val apks = release["assets"]?.jsonArray
            ?.mapNotNull { it as? JsonObject }
            ?.mapNotNull { asset ->
                val name = asset["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val url = asset["browser_download_url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val ours = name.startsWith(BuildConfig.UPDATE_APK_PREFIX, ignoreCase = true) &&
                    name.endsWith(".apk", ignoreCase = true) &&
                    asset["state"]?.jsonPrimitive?.contentOrNull == "uploaded"
                if (ours) name.removeSuffix(".apk").removeSuffix(".APK") to url else null
            }
            .orEmpty()
        apks.firstOrNull { (name, _) -> '_' !in name }?.second
            ?: Build.SUPPORTED_ABIS.firstNotNullOfOrNull { abi ->
                apks.firstOrNull { (name, _) -> name.endsWith("_$abi") }?.second
            }
    }.getOrNull()

    fun downloadApk(context: Context) {
        val info = _available.value ?: return
        val url = info.apkUrl ?: return
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return

        val target = getApkFile(context, info.version)
        if (target.exists()) {
            target.delete()
        }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Chordly v${info.version}")
            .setDescription("Downloading update...")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, target.name)

        val downloadId = dm.enqueue(request)
        getPrefs(context).edit()
            .putLong(KEY_DOWNLOAD_ID, downloadId)
            .putString(KEY_DOWNLOAD_VERSION, info.version)
            .apply()

        _download.value = DownloadState.Downloading(0f)
        startPolling(context, downloadId, info.version)
    }

    private fun startPolling(context: Context, downloadId: Long, version: String) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return@launch
            while (isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
                    when (status) {
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED -> {
                            val soFarIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val soFar = if (soFarIdx != -1) cursor.getLong(soFarIdx) else 0L
                            val total = if (totalIdx != -1) cursor.getLong(totalIdx) else 0L
                            val fraction = if (total > 0) (soFar.toFloat() / total).coerceIn(0f, 1f) else 0f
                            _download.value = DownloadState.Downloading(fraction)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            cursor.close()
                            val target = getApkFile(context, version)
                            _download.value = DownloadState.Ready(target)
                            return@launch
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = if (reasonIdx != -1) cursor.getInt(reasonIdx) else -1
                            cursor.close()
                            _download.value = DownloadState.Failed("Download failed ($reason)")
                            return@launch
                        }
                    }
                    cursor.close()
                } else {
                    cursor?.close()
                }
                delay(500)
            }
        }
    }

    fun onDownloadCompleted(context: Context, downloadId: Long) {
        val prefs = getPrefs(context)
        val savedId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
        if (savedId != downloadId) return

        val version = _available.value?.version ?: prefs.getString(KEY_DOWNLOAD_VERSION, null) ?: return
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
        val cursor = dm.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor != null && cursor.moveToFirst()) {
            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                pollingJob?.cancel()
                val target = getApkFile(context, version)
                _download.value = DownloadState.Ready(target)
            } else if (status == DownloadManager.STATUS_FAILED) {
                pollingJob?.cancel()
                _download.value = DownloadState.Failed("Download failed")
            }
            cursor.close()
        } else {
            cursor?.close()
        }
    }

    fun syncDownloadStatus(context: Context) {
        val prefs = getPrefs(context)
        val savedId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
        val savedVersion = prefs.getString(KEY_DOWNLOAD_VERSION, null)
        val currentVersion = _available.value?.version ?: savedVersion

        if (savedId == -1L || currentVersion == null) {
            return
        }

        val target = getApkFile(context, currentVersion)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
        val cursor = dm.query(DownloadManager.Query().setFilterById(savedId))
        if (cursor != null && cursor.moveToFirst()) {
            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (statusIdx != -1) cursor.getInt(statusIdx) else -1
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    if (target.exists() && target.length() > 0) {
                        _download.value = DownloadState.Ready(target)
                    }
                }
                DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED -> {
                    val soFarIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val soFar = if (soFarIdx != -1) cursor.getLong(soFarIdx) else 0L
                    val total = if (totalIdx != -1) cursor.getLong(totalIdx) else 0L
                    val fraction = if (total > 0) (soFar.toFloat() / total).coerceIn(0f, 1f) else 0f
                    _download.value = DownloadState.Downloading(fraction)
                    startPolling(context, savedId, currentVersion)
                }
                DownloadManager.STATUS_FAILED -> {
                    _download.value = DownloadState.Failed("Download failed")
                }
            }
            cursor.close()
        } else {
            cursor?.close()
            if (target.exists() && target.length() > 0) {
                _download.value = DownloadState.Ready(target)
            }
        }
    }

    fun cancelDownload(context: Context? = null) {
        pollingJob?.cancel()
        pollingJob = null
        context?.let { ctx ->
            val prefs = getPrefs(ctx)
            val savedId = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
            if (savedId != -1L) {
                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                dm?.remove(savedId)
            }
            val savedVersion = prefs.getString(KEY_DOWNLOAD_VERSION, null)
            if (savedVersion != null) {
                val file = getApkFile(ctx, savedVersion)
                if (file.exists()) file.delete()
            }
            prefs.edit().remove(KEY_DOWNLOAD_ID).remove(KEY_DOWNLOAD_VERSION).apply()
        }
        _download.value = DownloadState.Idle
    }

    fun resetDownload() {
        _download.value = DownloadState.Idle
    }

    fun installApk(context: Context, file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val opened = runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }.isSuccess
            if (opened) return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(
            Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                .putExtra(Intent.EXTRA_RETURN_RESULT, true)
                .addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                ),
        )
    }

    private data class ParsedVersion(val parts: List<Int>, val isPreRelease: Boolean)

    private fun parseVersion(raw: String): ParsedVersion {
        val dash = raw.indexOf('-')
        val base = if (dash >= 0) raw.substring(0, dash) else raw
        return ParsedVersion(base.split(".").map { it.toIntOrNull() ?: 0 }, dash >= 0)
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val l = parseVersion(latest)
        val c = parseVersion(current)
        for (i in 0 until maxOf(l.parts.size, c.parts.size)) {
            val a = l.parts.getOrElse(i) { 0 }
            val b = c.parts.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return c.isPreRelease && !l.isPreRelease
    }
}
