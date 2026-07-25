package com.example.loveyapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.loveyapp.data.service.DataBackupManager
import com.example.loveyapp.di.DataBackupManagerEntryPoint
import com.example.loveyapp.security.AuthService
import com.example.loveyapp.ui.navigation.AppNavigator
import com.example.loveyapp.ui.theme.LoveYaTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var authService: AuthService
    private lateinit var dataBackupManager: DataBackupManager
    private lateinit var databaseFactory: com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
    private var onExportResult: ((Boolean) -> Unit)? = null
    private var onImportResult: ((Boolean) -> Unit)? = null

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri: Uri? ->
        uri?.let {
            val success = dataBackupManager.exportDatabase(authService.currentUsername ?: "", it)
            onExportResult?.invoke(success)
        } ?: run {
            onExportResult?.invoke(false)
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val success = dataBackupManager.importDatabase(authService.currentUsername ?: "", it)
            onImportResult?.invoke(success)
        } ?: run {
            onImportResult?.invoke(false)
        }
    }

    private val storagePathLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            databaseFactory.setCustomStorageUri(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authService = EntryPointAccessors.fromApplication(
            applicationContext,
            com.example.loveyapp.di.AuthServiceEntryPoint::class.java
        ).authService()

        dataBackupManager = EntryPointAccessors.fromApplication(
            applicationContext,
            DataBackupManagerEntryPoint::class.java
        ).dataBackupManager()

        databaseFactory = EntryPointAccessors.fromApplication(
            applicationContext,
            com.example.loveyapp.di.LoveYaDatabaseFactoryEntryPoint::class.java
        ).databaseFactory()

        setContent {
            LoveYaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigator(
                        context = this,
                        onSelectStoragePath = {
                            storagePathLauncher.launch(null)
                        },
                        onStoragePathChanged = {
                            databaseFactory.closeAll()
                        },
                        onExportData = { onExport ->
                            onExportResult = onExport
                            val fileName = dataBackupManager.getBackupFileName(authService.currentUsername ?: "")
                            exportLauncher.launch(fileName)
                        },
                        onImportData = { onImport ->
                            onImportResult = onImport
                            importLauncher.launch(arrayOf("*/*"))
                        },
                        onDataReload = {
                            databaseFactory.closeAll()
                        }
                    )
                }
            }
        }
    }
}