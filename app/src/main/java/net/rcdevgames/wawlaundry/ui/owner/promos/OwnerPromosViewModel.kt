package net.rcdevgames.wawlaundry.ui.owner.promos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.PromoEntity
import net.rcdevgames.wawlaundry.data.local.entity.PromoType
import net.rcdevgames.wawlaundry.domain.repository.PromoRepository
import java.util.UUID
import javax.inject.Inject

data class OwnerPromosState(
    val promos: List<PromoEntity> = emptyList(),
    val isLoading: Boolean = true,
    
    // Add/Edit Form
    val showDialog: Boolean = false,
    val editPromoId: String? = null,
    val titleInput: String = "",
    val typeInput: PromoType = PromoType.PERCENTAGE,
    val valueInput: String = "",
    val isActiveInput: Boolean = true,
    
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerPromosViewModel @Inject constructor(
    private val promoRepository: PromoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerPromosState())
    val state: StateFlow<OwnerPromosState> = _state.asStateFlow()

    init {
        loadPromos()
    }

    private fun loadPromos() {
        viewModelScope.launch {
            promoRepository.getAllPromos().collect { list ->
                _state.update { it.copy(promos = list, isLoading = false) }
            }
        }
    }

    fun toggleDialog(show: Boolean, promoToEdit: PromoEntity? = null) {
        if (show && promoToEdit != null) {
            _state.update {
                it.copy(
                    showDialog = true,
                    editPromoId = promoToEdit.id,
                    titleInput = promoToEdit.title,
                    typeInput = promoToEdit.promoType,
                    valueInput = if (promoToEdit.promoType == PromoType.PERCENTAGE) 
                                     promoToEdit.value.toInt().toString() else promoToEdit.value.toLong().toString(),
                    isActiveInput = promoToEdit.isActive,
                    errorMessage = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    showDialog = show,
                    editPromoId = null,
                    titleInput = "",
                    typeInput = PromoType.PERCENTAGE,
                    valueInput = "",
                    isActiveInput = true,
                    errorMessage = null
                )
            }
        }
    }

    fun onTitleChange(title: String) = _state.update { it.copy(titleInput = title) }
    fun onTypeChange(type: PromoType) = _state.update { it.copy(typeInput = type) }
    fun onActiveChange(isActive: Boolean) = _state.update { it.copy(isActiveInput = isActive) }
    fun onValueChange(valueStr: String) {
        val filtered = valueStr.filter { it.isDigit() }
        _state.update { it.copy(valueInput = filtered) }
    }

    fun savePromo() {
        val currentState = _state.value
        
        if (currentState.titleInput.isBlank() || currentState.valueInput.isBlank()) {
            _state.update { it.copy(errorMessage = "Judul dan Nilai tidak boleh kosong") }
            return
        }

        val parsedValue = currentState.valueInput.toDoubleOrNull() ?: 0.0

        if (currentState.typeInput == PromoType.PERCENTAGE && (parsedValue <= 0 || parsedValue > 100)) {
            _state.update { it.copy(errorMessage = "Nilai Persentase harus antara 1% - 100%") }
            return
        }

        val promo = PromoEntity(
            id = currentState.editPromoId ?: UUID.randomUUID().toString(),
            userId = null,
            title = currentState.titleInput,
            promoType = currentState.typeInput,
            value = parsedValue,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + (86400000L * 365), // +1 Year Simple setup
            isActive = currentState.isActiveInput,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                promoRepository.savePromo(promo)
                toggleDialog(false)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun deletePromo(promoId: String) {
        viewModelScope.launch {
            try {
                promoRepository.softDeletePromo(promoId, System.currentTimeMillis())
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }
}
