package net.rcdevgames.wawlaundry.ui.setup

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

data class BusinessSetupState(
    val id: String = UUID.randomUUID().toString(),
    val businessName: String = "",
    val address: String = "",
    val phone: String = "",
    val headerText: String = "Selamat Datang di Laundry Kami",
    val footerText: String = "Terima Kasih!\nBarang yang tidak diambil dalam 30 hari bukan tanggung jawab kami.",
    val logoPath: String? = null,
    val themeColor: String = "#FF6200EE",
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BusinessSetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessSetupState())
    val state: StateFlow<BusinessSetupState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val existingProfile = profileRepository.getProfile().firstOrNull()
            if (existingProfile != null) {
                _state.update {
                    it.copy(
                        id = existingProfile.id,
                        businessName = existingProfile.businessName,
                        address = existingProfile.address,
                        phone = existingProfile.phone,
                        headerText = existingProfile.headerText,
                        footerText = existingProfile.footerText,
                        logoPath = existingProfile.logoPath,
                        themeColor = existingProfile.themeColor,
                        isDarkMode = existingProfile.isDarkMode,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBusinessNameChange(value: String) = _state.update { it.copy(businessName = value) }
    fun onAddressChange(value: String) = _state.update { it.copy(address = value) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value) }
    fun onHeaderChange(value: String) = _state.update { it.copy(headerText = value) }
    fun onFooterChange(value: String) = _state.update { it.copy(footerText = value) }
    fun onDarkModeToggle(isDark: Boolean) = _state.update { it.copy(isDarkMode = isDark) }

    fun saveProfile() {
        val currentState = _state.value
        if (currentState.businessName.isBlank() || currentState.phone.isBlank() || currentState.address.isBlank()) {
            _state.update { it.copy(errorMessage = "Nama Usaha, Alamat, dan No. HP wajib diisi") }
            return
        }

        val entity = ProfileEntity(
            id = currentState.id,
            userId = null, // Set during cloud sync or login
            businessName = currentState.businessName,
            address = currentState.address,
            phone = currentState.phone,
            headerText = currentState.headerText,
            footerText = currentState.footerText,
            logoPath = currentState.logoPath,
            themeColor = currentState.themeColor,
            isDarkMode = currentState.isDarkMode,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                profileRepository.saveProfile(entity)
                _state.update { it.copy(isSaved = true, errorMessage = null) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan profil: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
