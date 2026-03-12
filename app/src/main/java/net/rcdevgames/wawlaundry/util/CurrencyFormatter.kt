package net.rcdevgames.wawlaundry.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Singleton currency formatter to avoid creating new NumberFormat instances
 * on every Compose recomposition (which causes memory leaks).
 */
object CurrencyFormatter {

    private val indonesiaLocale = Locale.forLanguageTag("id-ID")

    // Reusable formatter instance (thread-safe after first configuration)
    @Volatile
    private var _formatter: NumberFormat? = null

    private val formatter: NumberFormat
        get() {
            return _formatter ?: synchronized(this) {
                _formatter ?: NumberFormat.getCurrencyInstance(indonesiaLocale).also {
                    _formatter = it
                }
            }
        }

    /**
     * Format a Double value to Indonesian Rupiah currency string.
     * Example: 150000 -> "Rp150.000"
     */
    fun format(amount: Double): String {
        return formatter.format(amount)
    }

    /**
     * Format an Int value to Indonesian Rupiah currency string.
     */
    fun format(amount: Int): String {
        return formatter.format(amount.toDouble())
    }

    /**
     * Format a Long value to Indonesian Rupiah currency string.
     */
    fun format(amount: Long): String {
        return formatter.format(amount.toDouble())
    }

    /**
     * Get the raw NumberFormat instance if custom formatting is needed.
     */
    fun getNumberFormat(): NumberFormat = formatter

    /**
     * Non-currency number formatter (for displaying quantities, etc.)
     */
    private var _numberFormatter: NumberFormat? = null

    private val numberFormatter: NumberFormat
        get() {
            return _numberFormatter ?: synchronized(this) {
                _numberFormatter ?: NumberFormat.getInstance(indonesiaLocale).also {
                    _numberFormatter = it
                }
            }
        }

    /**
     * Format a number without currency symbol.
     * Example: 150000 -> "150.000"
     */
    fun formatNumber(amount: Double): String {
        return numberFormatter.format(amount)
    }

    fun formatNumber(amount: Int): String {
        return numberFormatter.format(amount)
    }
}
