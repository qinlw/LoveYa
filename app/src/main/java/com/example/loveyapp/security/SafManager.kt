package com.example.loveyapp.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun createDirectoryPickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
    }

    fun createFilePickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun takePersistableUriPermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun saveFileToSaf(directoryUri: Uri, fileName: String, content: ByteArray): Boolean {
        return try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            var file = directory?.findFile(fileName)
            if (file == null) {
                file = directory?.createFile("application/json", fileName)
            }

            file?.let {
                context.contentResolver.openOutputStream(it.uri)?.use { outputStream ->
                    outputStream.write(content)
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readFileFromSaf(fileUri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyDatabaseToSaf(sourcePath: String, targetDirectoryUri: Uri, databaseName: String): Boolean {
        return try {
            val sourceFile = java.io.File(sourcePath)
            if (!sourceFile.exists()) return false

            val directory = DocumentFile.fromTreeUri(context, targetDirectoryUri)
            var targetFile = directory?.findFile(databaseName)
            if (targetFile == null) {
                targetFile = directory?.createFile("application/octet-stream", databaseName)
            }

            targetFile?.let {
                context.contentResolver.openOutputStream(it.uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDatabasePathFromSaf(directoryUri: Uri, databaseName: String): String? {
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        val file = directory?.findFile(databaseName)
        return file?.uri?.toString()
    }

    fun deleteFileFromSaf(directoryUri: Uri, fileName: String): Boolean {
        return try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            val file = directory?.findFile(fileName)
            file?.delete() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}