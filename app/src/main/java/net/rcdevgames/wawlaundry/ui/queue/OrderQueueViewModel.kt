package net.rcdevgames.wawlaundry.ui.queue

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.domain.repository.OrderRepository
import net.rcdevgames.wawlaundry.domain.repository.ProfileRepository
import net.rcdevgames.wawlaundry.domain.repository.CustomerRepository
import net.rcdevgames.wawlaundry.util.WhatsAppHelper
import net.rcdevgames.wawlaundry.util.ReceiptPdfGenerator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class OrderQueueState(
    val orders: List<OrderEntity> = emptyList(),
    val filteredOrders: List<OrderEntity> = emptyList(),
    val customers: Map<String, CustomerEntity> = emptyMap(),
    val selectedTab: OrderStatus = OrderStatus.QUEUE,
    val isLoading: Boolean = true,
    val isGeneratingPdf: Boolean = false,
    val pdfPath: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OrderQueueViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(OrderQueueState())
    val state: StateFlow<OrderQueueState> = _state.asStateFlow()

    init {
        loadOrders()
        loadCustomers()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { allOrders ->
                // Sort by entry time descending (newest on top)
                val sorted = allOrders.sortedByDescending { it.entryDate }
                _state.update {
                    it.copy(
                        orders = sorted,
                        isLoading = false,
                        filteredOrders = sorted.filter { order -> order.orderStatus == it.selectedTab }
                    )
                }
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                val customerMap = customers.associateBy { it.id }
                _state.update { it.copy(customers = customerMap) }
            }
        }
    }

    fun selectTab(status: OrderStatus) {
        val filtered = _state.value.orders.filter { it.orderStatus == status }
        _state.update { it.copy(selectedTab = status, filteredOrders = filtered) }
    }

    fun updateOrderStatus(order: OrderEntity, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                val exitDate = if (newStatus == OrderStatus.PICKED_UP) System.currentTimeMillis() else null
                val updatedOrder = order.copy(
                    orderStatus = newStatus,
                    exitDate = exitDate,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                orderRepository.updateOrder(updatedOrder)
                // Flow will automatically emit the new list and refresh the UI
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal memperbarui status: ${e.message}") }
            }
        }
    }

    fun sendWhatsAppNotification(
        order: OrderEntity,
        messageType: WhatsAppMessageType
    ) {
        viewModelScope.launch {
            try {
                val customer = customerRepository.getCustomerById(order.customerId)
                if (customer == null) {
                    _state.update { it.copy(errorMessage = "Data pelanggan tidak ditemukan") }
                    return@launch
                }

                if (customer.phone.isBlank()) {
                    _state.update { it.copy(errorMessage = "Pelanggan tidak memiliki nomor WhatsApp") }
                    return@launch
                }

                val profile = profileRepository.getProfile().firstOrNull()
                val businessName = profile?.businessName ?: "WAW LAUNDRY"

                val message = when (messageType) {
                    WhatsAppMessageType.DONE -> WhatsAppHelper.generateOrderDoneMessage(
                        customer = customer,
                        order = order,
                        businessName = businessName
                    )
                    WhatsAppMessageType.WASHING -> WhatsAppHelper.generateOrderWashingMessage(
                        customer = customer,
                        order = order,
                        businessName = businessName
                    )
                }

                WhatsAppHelper.sendWhatsAppMessage(context, customer.phone, message)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal mengirim WhatsApp: ${e.message}") }
            }
        }
    }

    suspend fun generateAndShareReceipt(order: OrderEntity): Result<String> {
        return try {
            _state.update { it.copy(isGeneratingPdf = true, pdfPath = null) }

            val customer = customerRepository.getCustomerById(order.customerId)
                ?: throw Exception("Data pelanggan tidak ditemukan")

            val details = orderRepository.getOrderDetails(order.id).first()
            val profile = profileRepository.getProfile().firstOrNull()

            val receiptData = ReceiptPdfGenerator.ReceiptData(
                businessName = profile?.businessName ?: "WAW LAUNDRY",
                businessPhone = profile?.phone ?: "",
                businessAddress = profile?.address ?: "",
                order = order,
                details = details,
                customer = customer,
                footerText = profile?.footerText ?: "Terima Kasih!"
            )

            val result = ReceiptPdfGenerator.generateReceiptPdf(context, receiptData)

            result.onSuccess { path ->
                _state.update { it.copy(isGeneratingPdf = false, pdfPath = path) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isGeneratingPdf = false,
                        errorMessage = "Gagal generate PDF: ${error.message}"
                    )
                }
            }

            result
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isGeneratingPdf = false,
                    errorMessage = "Gagal generate PDF: ${e.message}"
                )
            }
            Result.failure(e)
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearPdfPath() {
        _state.update { it.copy(pdfPath = null) }
    }

    fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }
}
