package net.rcdevgames.wawlaundry.ui.owner.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.AppDatabase
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class OwnerDataState(
    val showResetDialog: Boolean = false,
    val resetPinInput: String = "",
    val pinError: Boolean = false,
    val isResetting: Boolean = false,
    val isResetSuccess: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val errorMessage: String? = null,
    // Local backup/restore state
    val showRestoreDialog: Boolean = false,
    val restorePasswordInput: String = "",
    val restorePasswordError: Boolean = false,
    val restoreUri: Uri? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false
)

@HiltViewModel
class OwnerDataViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val securityPrefs: SecurityPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerDataState())
    val state: StateFlow<OwnerDataState> = _state.asStateFlow()

    // === LOCAL BACKUP (replaces cloud sync) ===
    // Cloud sync has been disabled to reduce APK size

    fun onBackupDataClick(onFilePicker: () -> Unit) {
        // Trigger file picker to choose where to save backup
        onFilePicker()
    }

    fun performBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isBackingUp = true, syncMessage = null, errorMessage = null) }

                // Export database to selected location
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    // Get the database file
                    val dbFile = context.getDatabasePath("waw_laundry_secure.db")

                    if (dbFile.exists()) {
                        FileInputStream(dbFile).use { input ->
                            input.copyTo(output)
                        }
                        _state.update { it.copy(isBackingUp = false, syncMessage = "Backup berhasil disimpan!") }
                    } else {
                        _state.update { it.copy(isBackingUp = false, errorMessage = "Database tidak ditemukan!") }
                    }
                } ?: run {
                    _state.update { it.copy(isBackingUp = false, errorMessage = "Gagal membuka file output!") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isBackingUp = false, errorMessage = "Gagal backup: ${e.message}") }
            }
        }
    }

    fun onRestoreDataClick(onFilePicker: () -> Unit) {
        // Trigger file picker to select backup file
        onFilePicker()
    }

    fun onRestoreFileSelected(uri: Uri) {
        // Show password dialog after file is selected
        _state.update { it.copy(showRestoreDialog = true, restoreUri = uri, restorePasswordInput = "", restorePasswordError = false) }
    }

    fun onRestorePasswordChange(password: String) {
        _state.update { it.copy(restorePasswordInput = password, restorePasswordError = false) }
    }

    fun onRestoreCancel() {
        _state.update { it.copy(showRestoreDialog = false, restoreUri = null, restorePasswordInput = "") }
    }

    fun confirmRestore() {
        val inputPassword = _state.value.restorePasswordInput
        val savedPassword = securityPrefs.getMasterPassword()

        // Verify master password before restoring
        if (inputPassword != savedPassword) {
            _state.update { it.copy(restorePasswordError = true) }
            return
        }

        // Proceed with restore
        val uri = _state.value.restoreUri
        if (uri == null) {
            _state.update { it.copy(showRestoreDialog = false, errorMessage = "File backup tidak dipilih!") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(showRestoreDialog = false, isRestoring = true, errorMessage = null) }

                // Close all database connections
                database.close()

                // Import database from selected location
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val dbFile = context.getDatabasePath("waw_laundry_secure.db")

                    // Ensure parent directory exists
                    dbFile.parentFile?.mkdirs()

                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }

                    // Database will be auto-reopened on next access
                    _state.update { it.copy(isRestoring = false, syncMessage = "Restore berhasil! Aplikasi akan dimuat ulang.", restoreUri = null) }
                } ?: run {
                    _state.update { it.copy(isRestoring = false, errorMessage = "Gagal membuka file input!", restoreUri = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isRestoring = false, errorMessage = "Gagal restore: ${e.message}", restoreUri = null) }
            }
        }
    }

    fun onResetRequest() {
        _state.update { it.copy(showResetDialog = true, resetPinInput = "", pinError = false) }
    }

    fun onResetCancel() {
        _state.update { it.copy(showResetDialog = false, resetPinInput = "") }
    }

    fun onPinChange(pin: String) {
        if (pin.length <= 6) { // Asumsi PIN 6 digit
            _state.update { it.copy(resetPinInput = pin, pinError = false) }
        }
    }

    fun confirmReset() {
        val currentPin = _state.value.resetPinInput
        val savedPin = securityPrefs.getOwnerPin()

        if (currentPin != savedPin) {
            _state.update { it.copy(pinError = true) }
            return
        }

        // Proceed to reset
        _state.update { it.copy(showResetDialog = false, isResetting = true) }

        viewModelScope.launch {
            try {
                // Hapus Database
                database.clearAllTables()
                
                // Hapus File Keamanan/Preferences
                securityPrefs.clearAll()
                
                // Note: Other shared prefs can be deleted here if needed
                
                _state.update { it.copy(isResetting = false, isResetSuccess = true) }

            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isResetting = false, 
                        errorMessage = "Gagal mereset data: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, syncMessage = null) }
    }
}
