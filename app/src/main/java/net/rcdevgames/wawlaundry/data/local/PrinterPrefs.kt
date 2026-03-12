package net.rcdevgames.wawlaundry.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    fun saveDefaultPrinterAddress(macAddress: String) {
        prefs.edit().putString(KEY_DEFAULT_PRINTER_ADDRESS, macAddress).apply()
    }

    fun getDefaultPrinterAddress(): String? {
        return prefs.getString(KEY_DEFAULT_PRINTER_ADDRESS, null)
    }

    var printerType: String
        get() = prefs.getString(KEY_PRINTER_TYPE, "Bluetooth") ?: "Bluetooth"
        set(value) = prefs.edit().putString(KEY_PRINTER_TYPE, value).apply()

    var paperSize: Int
        get() = prefs.getInt(KEY_PAPER_SIZE, 58)
        set(value) = prefs.edit().putInt(KEY_PAPER_SIZE, value).apply()

    var printGraphic: Boolean
        get() = prefs.getBoolean(KEY_PRINT_GRAPHIC, false)
        set(value) = prefs.edit().putBoolean(KEY_PRINT_GRAPHIC, value).apply()

    var printAfterTransaction: Boolean
        get() = prefs.getBoolean(KEY_PRINT_AFTER_TX, true)
        set(value) = prefs.edit().putBoolean(KEY_PRINT_AFTER_TX, value).apply()

    var printTwice: Boolean
        get() = prefs.getBoolean(KEY_PRINT_TWICE, false)
        set(value) = prefs.edit().putBoolean(KEY_PRINT_TWICE, value).apply()

    var cashDrawer: Boolean
        get() = prefs.getBoolean(KEY_CASH_DRAWER, false)
        set(value) = prefs.edit().putBoolean(KEY_CASH_DRAWER, value).apply()

    var longReceipt: Boolean
        get() = prefs.getBoolean(KEY_LONG_RECEIPT, false)
        set(value) = prefs.edit().putBoolean(KEY_LONG_RECEIPT, value).apply()

    var printerLanguage: String
        get() = prefs.getString(KEY_PRINTER_LANGUAGE, "Alfabet Standar") ?: "Alfabet Standar"
        set(value) = prefs.edit().putString(KEY_PRINTER_LANGUAGE, value).apply()

    companion object {
        private const val KEY_DEFAULT_PRINTER_ADDRESS = "default_printer_address"
        private const val KEY_PRINTER_TYPE = "printer_type"
        private const val KEY_PAPER_SIZE = "paper_size"
        private const val KEY_PRINT_GRAPHIC = "print_graphic"
        private const val KEY_PRINT_AFTER_TX = "print_after_tx"
        private const val KEY_PRINT_TWICE = "print_twice"
        private const val KEY_CASH_DRAWER = "cash_drawer"
        private const val KEY_LONG_RECEIPT = "long_receipt"
        private const val KEY_PRINTER_LANGUAGE = "printer_language"
    }
}
