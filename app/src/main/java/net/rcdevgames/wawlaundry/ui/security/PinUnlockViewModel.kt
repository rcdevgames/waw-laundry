package net.rcdevgames.wawlaundry.ui.security

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import javax.inject.Inject

data class PinUnlockState(
    val pinInput: String = "",
    val isError: Boolean = false,
    val isUnlocked: Boolean = false
)

@HiltViewModel
class PinUnlockViewModel @Inject constructor(
    private val securityPrefs: SecurityPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(PinUnlockState())
    val state: StateFlow<PinUnlockState> = _state.asStateFlow()

    fun onPinChange(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
            _state.update { it.copy(pinInput = pin, isError = false) }
            
            // Auto submit if 6 digits
            if (pin.length == 6) {
                verifyPin()
            }
        }
    }

    fun verifyPin() {
        val savedPin = securityPrefs.getOwnerPin()
        if (_state.value.pinInput == savedPin) {
            _state.update { it.copy(isUnlocked = true) }
        } else {
            _state.update { it.copy(isError = true, pinInput = "") }
        }
    }
}
