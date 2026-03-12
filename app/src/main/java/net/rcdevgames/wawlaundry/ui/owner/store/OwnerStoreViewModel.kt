package net.rcdevgames.wawlaundry.ui.owner.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.ProfileEntity
import net.rcdevgames.wawlaundry.domain.repository.ProfileRepository
import java.util.UUID
import javax.inject.Inject

data class OwnerStoreState(
    val businessName: String = "",
    val phone: String = "",
    val address: String = "",
    val headerText: String = "",
    val footerText: String = "",
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerStoreViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerStoreState(isLoading = true))
    val state: StateFlow<OwnerStoreState> = _state.asStateFlow()

    private var profileId: String = UUID.randomUUID().toString()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile().firstOrNull()
                if (profile != null) {
                    profileId = profile.id
                    _state.update {
                        it.copy(
                            businessName = profile.businessName,
                            phone = profile.phone,
                            address = profile.address,
                            headerText = profile.headerText,
                            footerText = profile.footerText,
                            isDarkMode = profile.isDarkMode,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal memuat profil: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onBusinessNameChange(name: String) {
        _state.update { it.copy(businessName = name, isSaved = false) }
    }

    fun onPhoneChange(phone: String) {
        _state.update { it.copy(phone = phone, isSaved = false) }
    }

    fun onAddressChange(address: String) {
        _state.update { it.copy(address = address, isSaved = false) }
    }

    fun onHeaderChange(header: String) {
        _state.update { it.copy(headerText = header, isSaved = false) }
    }

    fun onFooterChange(footer: String) {
        _state.update { it.copy(footerText = footer, isSaved = false) }
    }

    fun onDarkModeToggle(isDark: Boolean) {
        _state.update { it.copy(isDarkMode = isDark, isSaved = false) }
    }

    fun saveProfile() {
        val currentState = _state.value

        if (currentState.businessName.isBlank() || currentState.phone.isBlank()) {
            _state.update { it.copy(errorMessage = "Nama Laundry dan No HP harus diisi") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val profile = ProfileEntity(
                    id = profileId,
                    userId = null, // Set in Cloud sync
                    businessName = currentState.businessName,
                    address = currentState.address,
                    phone = currentState.phone,
                    headerText = currentState.headerText,
                    footerText = currentState.footerText,
                    logoPath = null,
                    themeColor = "#FF6200EE",
                    isDarkMode = currentState.isDarkMode,
                    isSynced = false,
                    updatedAt = System.currentTimeMillis()
                )

                profileRepository.saveProfile(profile)
                _state.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    fun clearSavedStatus() {
        _state.update { it.copy(isSaved = false) }
    }
}
