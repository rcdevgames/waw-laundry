package net.rcdevgames.wawlaundry.ui.printer

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import net.rcdevgames.wawlaundry.domain.printer.ThermalPrinterService
import net.rcdevgames.wawlaundry.domain.repository.CustomerRepository
import net.rcdevgames.wawlaundry.domain.repository.OrderRepository
import net.rcdevgames.wawlaundry.domain.repository.ProfileRepository
import net.rcdevgames.wawlaundry.domain.repository.ServiceRepository
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PrintPreviewState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val selectedDevice: BluetoothDevice? = null,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val isPrinting: Boolean = false,
    val printSuccess: Boolean = false,
    val receiptText: List<String> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PrintPreviewViewModel @Inject constructor(
    private val printerService: ThermalPrinterService,
    private val orderRepository: OrderRepository,
    private val profileRepository: ProfileRepository,
    private val serviceRepository: ServiceRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrintPreviewState())
    val state: StateFlow<PrintPreviewState> = _state.asStateFlow()

    private var headerText: String? = null
    private var footerText: String? = null

    fun loadPairedDevices() {
        viewModelScope.launch {
            try {
                val devices = printerService.getPairedDevices()
                _state.update { it.copy(pairedDevices = devices) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal memuat perangkat Bluetooth: ${e.message}") }
            }
        }
    }

    fun selectDevice(device: BluetoothDevice) {
        _state.update { it.copy(selectedDevice = device) }
    }

    fun connectToPrinter() {
        val device = _state.value.selectedDevice
        if (device == null) {
            _state.update { it.copy(errorMessage = "Pilih printer terlebih dahulu") }
            return
        }

        _state.update { it.copy(isConnecting = true, errorMessage = null) }

        viewModelScope.launch {
            val success = printerService.connect(device.address)
            _state.update { 
                it.copy(
                    isConnecting = false, 
                    isConnected = success,
                    errorMessage = if (success) null else "Gagal terhubung ke printer"
                ) 
            }
        }
    }

    fun generateReceiptText(orderId: String) {
        viewModelScope.launch {
            try {
                // Formatting logic normally goes here. Simplified for code structure:
                val order = orderRepository.getOrderById(orderId) ?: throw Exception("Order not found")
                val details = orderRepository.getOrderDetails(orderId).first()
                val profile = profileRepository.getProfile().firstOrNull()
                val customer = customerRepository.getCustomerById(order.customerId)

                headerText = profile?.businessName ?: "WAW LAUNDRY"
                footerText = profile?.footerText ?: "Terima Kasih!"

                val lines = mutableListOf<String>()
                val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.forLanguageTag("id-ID"))
                val currencyFormat = CurrencyFormatter.getNumberFormat()

                lines.add("Nota: ${order.orderNumber}")
                lines.add("Tgl: ${dateFormat.format(Date(order.entryDate))}")
                lines.add("--------------------------------")

                // Customer Information
                if (customer != null) {
                    lines.add("Pelanggan: ${customer.name}")
                    if (customer.phone.isNotBlank()) {
                        lines.add("WhatsApp: ${customer.phone}")
                    }
                    lines.add("--------------------------------")
                }

                // Delivery Type
                if (order.deliveryType == net.rcdevgames.wawlaundry.data.local.entity.DeliveryType.DELIVERED) {
                    lines.add("(DIANTAR)")
                } else {
                    lines.add("(AMBIL SENDIRI)")
                }
                lines.add("--------------------------------")

                details.forEach { detail ->
                    lines.add("${detail.qty}x Service (${currencyFormat.format(detail.priceSnapshot)})")
                }
                lines.add("--------------------------------")

                // Subtotal and discount breakdown
                val servicesTotal = order.totalPrice + order.discountAmount
                if (order.discountAmount > 0) {
                    lines.add("Subtotal: ${currencyFormat.format(servicesTotal)}")
                    lines.add("Diskon: -${currencyFormat.format(order.discountAmount)}")
                }

                // Delivery Fee (if any)
                val deliveryFee = if (order.deliveryType == net.rcdevgames.wawlaundry.data.local.entity.DeliveryType.DELIVERED) {
                    order.totalPrice - (servicesTotal - order.discountAmount)
                } else {
                    0.0
                }
                if (deliveryFee > 0) {
                    lines.add("Ongkir: ${currencyFormat.format(deliveryFee)}")
                }

                lines.add("Total: ${currencyFormat.format(order.totalPrice)}")

                // Payment Status
                val paymentStatusText = when (order.paymentStatus) {
                    net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus.PAID -> "LUNAS"
                    net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus.PARTIAL -> "DP: ${currencyFormat.format(order.downPayment)}"
                    net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus.UNPAID -> "BELUM BAYAR"
                }
                lines.add("Status: $paymentStatusText")

                // Notes / Address
                if (!order.notes.isNullOrBlank()) {
                    lines.add("--------------------------------")
                    lines.add("Catatan/Alamat:")
                    // Word wrap for long notes
                    order.notes.chunked(32).forEach { chunk ->
                        lines.add(chunk)
                    }
                }

                _state.update { it.copy(receiptText = lines) }

            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Gagal memuat detail nota: ${e.message}") }
            }
        }
    }

    fun print() {
        if (!_state.value.isConnected) {
            _state.update { it.copy(errorMessage = "Printer belum terhubung!") }
            return
        }

        _state.update { it.copy(isPrinting = true, printSuccess = false) }

        viewModelScope.launch {
            printerService.printReceipt(_state.value.receiptText, headerText, footerText)
            
            // Dummy delay to simulate printing time
            kotlinx.coroutines.delay(2000)
            
            _state.update { it.copy(isPrinting = false, printSuccess = true) }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        printerService.disconnect()
    }
}
