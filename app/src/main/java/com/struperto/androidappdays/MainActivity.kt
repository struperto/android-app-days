package com.struperto.androidappdays

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.IntentCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.struperto.androidappdays.feature.start.AreaImportKind
import com.struperto.androidappdays.feature.start.AreaImportDraft
import com.struperto.androidappdays.feature.start.buildAreaImportCaptureText
import com.struperto.androidappdays.feature.start.resolveDocumentImportDrafts
import com.struperto.androidappdays.feature.start.buildLinkImportState
import com.struperto.androidappdays.feature.start.buildTextImportState
import com.struperto.androidappdays.ui.theme.DaysTheme
import kotlinx.coroutines.launch

private val SharedUrlRegex = Regex("""https?://\S+""")

private data class IncomingImportDraft(
    val kind: AreaImportKind,
    val title: String,
    val detail: String,
    val reference: String,
)

class MainActivity : ComponentActivity() {
    private val appContainer: AppContainer
        get() = (application as DaysApp).appContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DaysTheme {
                AndroidAppDaysApp()
            }
        }
        if (savedInstanceState == null) {
            persistIncomingSharedContent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        persistIncomingSharedContent(intent)
    }

    private fun persistIncomingSharedContent(intent: Intent?) {
        val senderLabel = resolveSenderLabel()
        val drafts = extractSharedImports(intent).ifEmpty { return }
        lifecycleScope.launch {
            drafts.forEach { draft ->
                appContainer.captureRepository.createTextCapture(
                    text = buildAreaImportCaptureText(
                        kind = draft.kind,
                        title = draft.title,
                        detail = draft.detailWithSender(senderLabel),
                        reference = draft.reference,
                    ),
                    areaId = null,
                )
            }
        }
    }

    private fun extractSharedImports(intent: Intent?): List<IncomingImportDraft> {
        if (intent == null) return emptyList()
        return when (intent.action) {
            Intent.ACTION_SEND -> extractSingleSharedImports(intent)
            Intent.ACTION_SEND_MULTIPLE -> extractMultipleSharedImports(intent)
            else -> emptyList()
        }
    }

    private fun extractSingleSharedImports(intent: Intent): List<IncomingImportDraft> {
        val mimeType = intent.type.orEmpty()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
        val streamUri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        val clipUri = intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
        val uriToImport = streamUri ?: clipUri
        if (uriToImport != null) {
            return uriImportDrafts(
                uri = uriToImport,
                mimeType = detectMimeType(uriToImport, mimeType),
            )
        }
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            .orEmpty()
            .ifBlank { intent.getStringExtra(Intent.EXTRA_HTML_TEXT).orEmpty() }
            .trim()
        if (sharedText.isBlank()) return emptyList()
        val linkMatch = SharedUrlRegex.find(sharedText)?.value
        if (linkMatch != null) {
            val (defaultTitle, detail) = buildLinkImportState(linkMatch)
            return listOf(IncomingImportDraft(
                kind = AreaImportKind.Link,
                title = subject.ifBlank { defaultTitle },
                detail = detail,
                reference = linkMatch,
            ))
        }
        val (title, detail) = buildTextImportState(
            text = sharedText,
            subject = subject,
        )
        return listOf(IncomingImportDraft(
            kind = AreaImportKind.Text,
            title = title,
            detail = detail,
            reference = sharedText,
        ))
    }

    private fun extractMultipleSharedImports(intent: Intent): List<IncomingImportDraft> {
        val mimeType = intent.type.orEmpty()
        val uris = IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        val clipUris = buildList {
            val clipData = intent.clipData ?: return@buildList
            repeat(clipData.itemCount) { index ->
                clipData.getItemAt(index).uri?.let(::add)
            }
        }
        return (uris.orEmpty() + clipUris).distinct().map { uri ->
            uriImportDrafts(
                uri = uri,
                mimeType = detectMimeType(uri, mimeType),
            )
        }.flatten()
    }

    private fun uriImportDrafts(
        uri: Uri,
        mimeType: String,
    ): List<IncomingImportDraft> {
        val isImage = mimeType.startsWith("image/")
        if (!isImage) {
            val connectorDrafts = resolveDocumentImportDrafts(
                uri = uri,
                fallbackMimeType = mimeType,
            )
            if (connectorDrafts.any { it.kind != AreaImportKind.File }) {
                return connectorDrafts.map(AreaImportDraft::toIncomingDraft)
            }
        }
        val title = resolveSharedDisplayName(uri).ifBlank { if (isImage) "Geteiltes Bild" else "Geteilte Datei" }
        val kind = if (isImage) AreaImportKind.Image else AreaImportKind.File
        return listOf(IncomingImportDraft(
            kind = kind,
            title = title,
            detail = if (isImage) {
                "Bild aus einer anderen App geteilt."
            } else {
                "Datei aus einer anderen App geteilt."
            },
            reference = uri.toString(),
        ))
    }

    private fun resolveSharedDisplayName(uri: Uri): String {
        val cursor: Cursor = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return uri.lastPathSegment.orEmpty()
        return cursor.use {
            if (!it.moveToFirst()) return@use uri.lastPathSegment.orEmpty()
            val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex < 0) uri.lastPathSegment.orEmpty() else it.getString(columnIndex).orEmpty()
        }
    }

    private fun detectMimeType(
        uri: Uri,
        fallback: String,
    ): String {
        return contentResolver.getType(uri).orEmpty().ifBlank { fallback }
    }

    private fun resolveSenderLabel(): String? {
        val senderPackage = referrer
            ?.host
            ?.takeIf(String::isNotBlank)
            ?: referrer
                ?.schemeSpecificPart
                ?.takeIf(String::isNotBlank)
            ?: return null
        val appInfo = runCatching {
            packageManager.getApplicationInfo(senderPackage, PackageManager.ApplicationInfoFlags.of(0))
        }.getOrElse {
            runCatching { packageManager.getApplicationInfo(senderPackage, 0) }.getOrNull()
        } ?: return senderPackage
        return packageManager.getApplicationLabel(appInfo).toString().ifBlank { senderPackage }
    }
}

private fun IncomingImportDraft.detailWithSender(
    senderLabel: String?,
): String {
    return senderLabel?.takeIf(String::isNotBlank)?.let { sender ->
        "$detail Von $sender geteilt."
    } ?: detail
}

private fun AreaImportDraft.toIncomingDraft(): IncomingImportDraft {
    return IncomingImportDraft(
        kind = kind,
        title = title,
        detail = detail,
        reference = reference,
    )
}
