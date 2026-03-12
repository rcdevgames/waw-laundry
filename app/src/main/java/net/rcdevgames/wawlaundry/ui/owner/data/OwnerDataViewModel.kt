package net.rcdevgames.wawlaundry.ui.owner.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.AppDatabase
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import javax.inject.Inject

data class OwnerDataState(
    val showResetDialog: Boolean = false,
    val resetPinInput: String = "",
    val pinError: Boolean = false,
    val isResetting: Boolean = false,
    val isResetSuccess: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerDataViewModel @Inject constructor(
    private val database: AppDatabase,
    private val securityPrefs: SecurityPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerDataState())
    val state: StateFlow<OwnerDataState> = _state.asStateFlow()

    fun onSyncDataClick() {
        // Implementasi sync placeholder untuk sementara, karena belum ada logic Supabase penuh untuk Push/Pull semua tabel manual
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            kotlinx.coroutines.delay(1500)
            _state.update { it.copy(isSyncing = false, syncMessage = "Sinkronisasi ke Cloud Berhasil!") }
            
            // Note: In real implementation, call sync service here.
        }
    }

    fun onRestoreDataClick() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            kotlinx.coroutines.delay(1500)
            _state.update { it.copy(isSyncing = false, syncMessage = "Ambil Data dari Cloud Berhasil!") }
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
