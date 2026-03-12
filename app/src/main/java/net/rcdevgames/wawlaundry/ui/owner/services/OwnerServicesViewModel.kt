package net.rcdevgames.wawlaundry.ui.owner.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import net.rcdevgames.wawlaundry.domain.repository.ServiceRepository
import java.util.UUID
import javax.inject.Inject

data class OwnerServicesState(
    val services: List<ServiceEntity> = emptyList(),
    val isLoading: Boolean = true,
    
    // Add/Edit Form
    val showDialog: Boolean = false,
    val editServiceId: String? = null,
    val nameInput: String = "",
    val priceInput: String = "",
    val categoryIdInput: String = "DEFAULT",
    
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerServicesViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerServicesState())
    val state: StateFlow<OwnerServicesState> = _state.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            serviceRepository.getAllServices().collect { list ->
                _state.update { it.copy(services = list, isLoading = false) }
            }
        }
    }

    fun toggleDialog(show: Boolean, serviceToEdit: ServiceEntity? = null) {
        if (show && serviceToEdit != null) {
            _state.update {
                it.copy(
                    showDialog = true,
                    editServiceId = serviceToEdit.id,
                    nameInput = serviceToEdit.name,
                    priceInput = serviceToEdit.price.toLong().toString(),
                    categoryIdInput = serviceToEdit.categoryId,
                    errorMessage = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    showDialog = show,
                    editServiceId = null,
                    nameInput = "",
                    priceInput = "",
                    categoryIdInput = "DEFAULT",
                    errorMessage = null
                )
            }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(nameInput = name) }
    fun onPriceChange(priceStr: String) {
        val filtered = priceStr.filter { it.isDigit() }
        _state.update { it.copy(priceInput = filtered) }
    }

    fun saveService() {
        val currentState = _state.value
        
        if (currentState.nameInput.isBlank() || currentState.priceInput.isBlank()) {
            _state.update { it.copy(errorMessage = "Nama dan Harga tidak boleh kosong") }
            return
        }

        val price = currentState.priceInput.toDoubleOrNull() ?: 0.0

        val service = ServiceEntity(
            id = currentState.editServiceId ?: UUID.randomUUID().toString(),
            userId = null,
            name = currentState.nameInput,
            price = price,
            categoryId = currentState.categoryIdInput,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                serviceRepository.saveService(service)
                toggleDialog(false)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                serviceRepository.deleteService(serviceId)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menghapus: ${e.message}") }
            }
        }
    }
    
    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
