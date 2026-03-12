package net.rcdevgames.wawlaundry.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import javax.inject.Inject

data class SetupSecurityState(
    val masterPassword: String = "",
    val masterPasswordConfirm: String = "",
    val ownerPin: String = "",
    val ownerPinConfirm: String = "",
    val isPasswordMatch: Boolean = true,
    val isPinMatch: Boolean = true,
    val isSetupComplete: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class SetupSecurityViewModel @Inject constructor(
    private val securityPrefs: SecurityPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(SetupSecurityState())
    val state: StateFlow<SetupSecurityState> = _state.asStateFlow()

    fun onMasterPasswordChange(password: String) {
        _state.update { it.copy(masterPassword = password, isPasswordMatch = password == it.masterPasswordConfirm) }
    }

    fun onMasterPasswordConfirmChange(password: String) {
        _state.update { it.copy(masterPasswordConfirm = password, isPasswordMatch = password == it.masterPassword) }
    }

    fun onOwnerPinChange(pin: String) {
        // Only allow numbers and max 6 digits
        if (pin.all { it.isDigit() } && pin.length <= 6) {
            _state.update { it.copy(ownerPin = pin, isPinMatch = pin == it.ownerPinConfirm) }
        }
    }

    fun onOwnerPinConfirmChange(pin: String) {
        if (pin.all { it.isDigit() } && pin.length <= 6) {
            _state.update { it.copy(ownerPinConfirm = pin, isPinMatch = pin == it.ownerPin) }
        }
    }

    fun saveSecuritySettings() {
        val currentState = _state.value
        
        if (currentState.masterPassword.isBlank() || currentState.ownerPin.isBlank()) {
            _state.update { it.copy(errorMessage = "Semua kolom harus diisi") }
            return
        }
        
        if (currentState.masterPassword.length < 6) {
            _state.update { it.copy(errorMessage = "Master Password minimal 6 karakter") }
            return
        }

        if (currentState.ownerPin.length != 6) {
             _state.update { it.copy(errorMessage = "PIN harus 6 digit") }
            return
        }

        if (!currentState.isPasswordMatch || !currentState.isPinMatch) {
            _state.update { it.copy(errorMessage = "Kombinasi Password atau PIN tidak cocok") }
            return
        }

        viewModelScope.launch {
            try {
                // Save to EncryptedSharedPreferences
                securityPrefs.saveMasterPassword(currentState.masterPassword)
                securityPrefs.saveOwnerPin(currentState.ownerPin)
                
                _state.update { it.copy(isSetupComplete = true, errorMessage = null) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan keamanan: ${e.message}") }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
