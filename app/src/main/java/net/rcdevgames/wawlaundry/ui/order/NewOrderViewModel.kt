package net.rcdevgames.wawlaundry.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.DeliveryType
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentMethod
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.data.local.entity.PromoEntity
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import net.rcdevgames.wawlaundry.domain.repository.CustomerRepository
import net.rcdevgames.wawlaundry.domain.repository.OrderRepository
import net.rcdevgames.wawlaundry.domain.repository.PromoRepository
import net.rcdevgames.wawlaundry.domain.repository.ServiceRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class NewOrderState(
    val customers: List<CustomerEntity> = emptyList(),
    val services: List<ServiceEntity> = emptyList(),
    val promos: List<PromoEntity> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val selectedServices: Map<ServiceEntity, Float> = emptyMap(), // Service to Quantity
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    
    // New Customer Form
    val showNewCustomerDialog: Boolean = false,
    val newCustomerName: String = "",
    val newCustomerPhone: String = "",
    val errorMessage: String? = null,

    // Checkout Details
    val selectedPromo: PromoEntity? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val deliveryType: DeliveryType = DeliveryType.SELF_PICKUP,
    val downPaymentText: String = "",
    val deliveryFeeText: String = "",
    val notes: String = "",
    // Bottom Sheet Visibilities for Unified POS Screen
    val showCustomerSheet: Boolean = false,
    val showCheckoutSheet: Boolean = false,
    
    val isOrderSaved: Boolean = false
)

@HiltViewModel
class NewOrderViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val serviceRepository: ServiceRepository,
    private val promoRepository: PromoRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewOrderState())
    val state: StateFlow<NewOrderState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                customerRepository.getAllCustomers().collect { customers ->
                    _state.update { it.copy(customers = customers) }
                }
            }
            launch {
                serviceRepository.getAllServices().collect { services ->
                    _state.update { it.copy(services = services, isLoading = false) }
                }
            }
            launch {
                val now = System.currentTimeMillis()
                promoRepository.getActivePromos(now).collect { promos ->
                    _state.update { it.copy(promos = promos) }
                }
            }
        }
    }

    // Customer & Service Actions (Truncated/reused from previous logic)
    fun onSearchQueryChange(query: String) = _state.update { it.copy(searchQuery = query) }
    fun selectCustomer(customer: CustomerEntity) = _state.update { it.copy(selectedCustomer = customer) }
    fun deselectCustomer() = _state.update { it.copy(selectedCustomer = null) }
    
    fun toggleNewCustomerDialog(show: Boolean) = _state.update { 
        it.copy(showNewCustomerDialog = show, newCustomerName = "", newCustomerPhone = "", errorMessage = null) 
    }
    fun onNewCustomerNameChange(name: String) = _state.update { it.copy(newCustomerName = name) }
    fun onNewCustomerPhoneChange(phone: String) = _state.update { it.copy(newCustomerPhone = phone) }

    fun saveNewCustomer() {
        val currentState = _state.value
        if (currentState.newCustomerName.isBlank() || currentState.newCustomerPhone.isBlank()) {
            _state.update { it.copy(errorMessage = "Nama dan No. HP harus diisi") }
            return
        }

        val newCustomer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            userId = null,
            name = currentState.newCustomerName,
            phone = currentState.newCustomerPhone,
            address = null,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                customerRepository.saveCustomer(newCustomer)
                _state.update { 
                    it.copy(
                        showNewCustomerDialog = false, 
                        selectedCustomer = newCustomer,
                        errorMessage = null
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan: ${e.message}") }
            }
        }
    }

    fun addService(service: ServiceEntity, qty: Float = 1f) {
        _state.update { state ->
            val updatedServices = state.selectedServices.toMutableMap()
            val currentQty = updatedServices[service] ?: 0f
            updatedServices[service] = currentQty + qty
            state.copy(selectedServices = updatedServices)
        }
    }

    fun decreaseService(service: ServiceEntity, qty: Float = 1f) {
        _state.update { state ->
            val updatedServices = state.selectedServices.toMutableMap()
            val currentQty = updatedServices[service] ?: 0f
            if (currentQty - qty <= 0) {
                updatedServices.remove(service)
            } else {
                updatedServices[service] = currentQty - qty
            }
            state.copy(selectedServices = updatedServices)
        }
    }

    // Sheet Toggles
    fun toggleCustomerSheet(show: Boolean) = _state.update { it.copy(showCustomerSheet = show) }
    fun toggleCheckoutSheet(show: Boolean) = _state.update { it.copy(showCheckoutSheet = show) }

    // Checkout Actions
    fun selectPromo(promo: PromoEntity?) = _state.update { it.copy(selectedPromo = promo) }
    fun setPaymentMethod(method: PaymentMethod) = _state.update { it.copy(paymentMethod = method) }
    fun setPaymentStatus(status: PaymentStatus) {
        _state.update { 
            // If marked as UNPAID or PAID, clear down payment text to avoid confusion
            val dp = if (status == PaymentStatus.PARTIAL) it.downPaymentText else ""
            it.copy(paymentStatus = status, downPaymentText = dp) 
        }
    }
    fun setDeliveryType(type: DeliveryType) = _state.update { it.copy(deliveryType = type) }
    fun onNotesChange(notes: String) = _state.update { it.copy(notes = notes) }
    fun onDownPaymentChange(dpUrl: String) {
        val filtered = dpUrl.filter { it.isDigit() }
        _state.update { it.copy(downPaymentText = filtered) }
    }
    fun onDeliveryFeeChange(fee: String) {
        val filtered = fee.filter { it.isDigit() }
        _state.update { it.copy(deliveryFeeText = filtered) }
    }

    fun calculateSubTotal(): Double {
        return _state.value.selectedServices.entries.sumOf { (service, qty) ->
            service.price * qty
        }
    }

    fun calculateDiscount(subTotal: Double): Double {
        val promo = _state.value.selectedPromo ?: return 0.0
        return if (promo.promoType == net.rcdevgames.wawlaundry.data.local.entity.PromoType.PERCENTAGE) {
            subTotal * (promo.value / 100.0)
        } else {
            if (promo.value > subTotal) subTotal else promo.value
        }
    }

    fun calculateTotal(): Double {
        val subTotal = calculateSubTotal()
        val discount = calculateDiscount(subTotal)
        val deliveryFee = _state.value.deliveryFeeText.toDoubleOrNull() ?: 0.0
        return subTotal - discount + deliveryFee
    }

    fun getDeliveryFee(): Double {
        return _state.value.deliveryFeeText.toDoubleOrNull() ?: 0.0
    }

    fun processOrder() {
        val currentState = _state.value
        if (currentState.selectedCustomer == null || currentState.selectedServices.isEmpty()) {
            _state.update { it.copy(errorMessage = "Gagal memproses. Data pelanggan/layanan kosong.") }
            return
        }

        // Validate: Notes is mandatory when delivery is selected
        if (currentState.deliveryType == DeliveryType.DELIVERED && currentState.notes.isBlank()) {
            _state.update { it.copy(errorMessage = "Mohon isi alamat pengiriman pada catatan") }
            return
        }

        val subTotal = calculateSubTotal()
        val discount = calculateDiscount(subTotal)
        val deliveryFee = currentState.deliveryFeeText.toDoubleOrNull() ?: 0.0
        val finalTotal = subTotal - discount + deliveryFee

        val dpValue = currentState.downPaymentText.toDoubleOrNull() ?: 0.0

        if (currentState.paymentStatus == PaymentStatus.PARTIAL && dpValue <= 0) {
            _state.update { it.copy(errorMessage = "Masukkan nominal Down Payment (DP)") }
            return
        }
        if (currentState.paymentStatus == PaymentStatus.PARTIAL && dpValue >= finalTotal) {
             _state.update { it.copy(errorMessage = "DP tidak boleh melebihi atau sama dengan Total. Gunakan status Lunas.") }
             return
        }

        val orderId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val orderNo = "TX" + SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date(now))

        val order = OrderEntity(
            id = orderId,
            userId = null,
            customerId = currentState.selectedCustomer.id,
            promoId = currentState.selectedPromo?.id,
            orderNumber = orderNo,
            entryDate = now,
            estimatedCompletionDate = now + (86400000 * 2), // Default +2 Days (could be dynamic)
            exitDate = null,
            totalPrice = finalTotal, // Includes delivery fee
            discountAmount = discount,
            downPayment = if (currentState.paymentStatus == PaymentStatus.PARTIAL) dpValue else 0.0,
            paymentStatus = currentState.paymentStatus,
            orderStatus = OrderStatus.QUEUE,
            paymentMethod = currentState.paymentMethod,
            deliveryType = currentState.deliveryType,
            notes = currentState.notes.takeIf { it.isNotBlank() },
            updatedAt = now
        )

        val orderDetails = currentState.selectedServices.map { (service, qty) ->
            OrderDetailEntity(
                id = UUID.randomUUID().toString(),
                orderId = orderId,
                serviceId = service.id,
                qty = qty,
                priceSnapshot = service.price,
                notes = null,
                updatedAt = now
            )
        }

        viewModelScope.launch {
            try {
                orderRepository.saveCompleteOrder(order, orderDetails)
                
                // Increment customer total orders
                customerRepository.incrementOrderCount(currentState.selectedCustomer.id)
                
                _state.update { it.copy(isOrderSaved = true, errorMessage = null) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal menyimpan pesanan: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
