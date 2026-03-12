package net.rcdevgames.wawlaundry.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudSyncState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null
)

// NOTE: Cloud sync disabled - using local backup only with master password verification
// Supabase dependencies removed to reduce APK size
@HiltViewModel
class CloudSyncSetupViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CloudSyncState())
    val state: StateFlow<CloudSyncState> = _state.asStateFlow()

    fun onEmailChange(email: String) = _state.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _state.update { it.copy(password = password) }

    fun login() {
        // Cloud sync disabled - show message to use local backup instead
        _state.update {
            it.copy(
                errorMessage = "Sinkronisasi cloud dinonaktifkan. Gunakan menu Backup/Restore untuk menyimpan data lokal."
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
