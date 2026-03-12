package net.rcdevgames.wawlaundry.ui.owner.printer

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.PrinterPrefs
import net.rcdevgames.wawlaundry.domain.printer.ThermalPrinterService
import javax.inject.Inject

data class OwnerPrinterState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val defaultPrinterAddress: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isTestPrintSuccess: Boolean = false,
    val errorMessage: String? = null,
    
    val printerType: String = "Bluetooth",
    val paperSize: Int = 58,
    val printGraphic: Boolean = false,
    val printAfterTransaction: Boolean = true,
    val printTwice: Boolean = false,
    val cashDrawer: Boolean = false,
    val longReceipt: Boolean = false,
    val printerLanguage: String = "Alfabet Standar"
)

@HiltViewModel
class OwnerPrinterViewModel @Inject constructor(
    private val printerService: ThermalPrinterService,
    private val printerPrefs: PrinterPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerPrinterState(isLoading = false))
    val state: StateFlow<OwnerPrinterState> = _state.asStateFlow()

    fun loadPrinters() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val devices = printerService.getPairedDevices()
                val currentDefault = printerPrefs.getDefaultPrinterAddress()
                
                _state.update { 
                    it.copy(
                        pairedDevices = devices,
                        defaultPrinterAddress = currentDefault,
                        printerType = printerPrefs.printerType,
                        paperSize = printerPrefs.paperSize,
                        printGraphic = printerPrefs.printGraphic,
                        printAfterTransaction = printerPrefs.printAfterTransaction,
                        printTwice = printerPrefs.printTwice,
                        cashDrawer = printerPrefs.cashDrawer,
                        longReceipt = printerPrefs.longReceipt,
                        printerLanguage = printerPrefs.printerLanguage,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        errorMessage = "Gagal memuat daftar perangkat Bluetooth: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun setDefaultPrinter(device: BluetoothDevice) {
        viewModelScope.launch {
            try {
                printerPrefs.saveDefaultPrinterAddress(device.address)
                _state.update { 
                    it.copy(
                        defaultPrinterAddress = device.address,
                        isSaved = true
                    ) 
                }
            } catch (e: Exception) {
                 _state.update { 
                    it.copy(errorMessage = "Gagal menyimpan pengaturan: ${e.message}") 
                }
            }
        }
    }

    fun onPrinterTypeChange(type: String) {
        printerPrefs.printerType = type
        _state.update { it.copy(printerType = type) }
    }

    fun onPaperSizeChange(size: Int) {
        printerPrefs.paperSize = size
        _state.update { it.copy(paperSize = size) }
    }

    fun onPrintGraphicChange(enabled: Boolean) {
        printerPrefs.printGraphic = enabled
        _state.update { it.copy(printGraphic = enabled) }
    }

    fun onPrintAfterTransactionChange(enabled: Boolean) {
        printerPrefs.printAfterTransaction = enabled
        _state.update { it.copy(printAfterTransaction = enabled) }
    }

    fun onPrintTwiceChange(enabled: Boolean) {
        printerPrefs.printTwice = enabled
        _state.update { it.copy(printTwice = enabled) }
    }

    fun onCashDrawerChange(enabled: Boolean) {
        printerPrefs.cashDrawer = enabled
        _state.update { it.copy(cashDrawer = enabled) }
    }

    fun onLongReceiptChange(enabled: Boolean) {
        printerPrefs.longReceipt = enabled
        _state.update { it.copy(longReceipt = enabled) }
    }

    fun onPrinterLanguageChange(language: String) {
        printerPrefs.printerLanguage = language
        _state.update { it.copy(printerLanguage = language) }
    }

    fun testPrint() {
        viewModelScope.launch {
            try {
                val address = printerPrefs.getDefaultPrinterAddress()
                if (address == null) {
                    _state.update { it.copy(errorMessage = "Silakan pilih printer default terlebih dahulu") }
                    return@launch
                }
                
                _state.update { it.copy(isLoading = true) }
                val connected = printerService.connect(address)
                if (connected) {
                    val lines = listOf(
                        "--- TEST PRINT ---",
                        "Waw Laundry",
                        "Printer terhubung dengan sukses!",
                        "Tipe: ${printerPrefs.printerType}",
                        "Kertas: ${printerPrefs.paperSize}mm",
                        "------------------"
                    )
                    printerService.printReceipt(lines, null, null)
                    printerService.disconnect()
                    _state.update { it.copy(isLoading = false, isTestPrintSuccess = true) }
                } else {
                    _state.update { it.copy(errorMessage = "Gagal terhubung ke printer. Pastikan printer menyala.", isLoading = false) }
                }
            } catch (e: Exception) {
               _state.update { it.copy(errorMessage = "Error: ${e.message}", isLoading = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
    fun clearSavedStatus() {
        _state.update { it.copy(isSaved = false) }
    }

    fun clearTestPrintSuccessStatus() {
        _state.update { it.copy(isTestPrintSuccess = false) }
    }
}
