package net.rcdevgames.wawlaundry.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.DeliveryType
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppHelper {

    fun sendWhatsAppMessage(
        context: Context,
        phoneNumber: String,
        message: String
    ) {
        // Clean phone number - remove non-digit characters and ensure format
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")

        // Indonesia country code (62) - if starts with 0, replace with 62
        val formattedPhone = if (cleanPhone.startsWith("0")) {
            "62${cleanPhone.substring(1)}"
        } else if (!cleanPhone.startsWith("62")) {
            "62$cleanPhone"
        } else {
            cleanPhone
        }

        // Encode message for URL
        val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")

        // Create WhatsApp URL
        val whatsappUrl = "https://wa.me/$formattedPhone?text=$encodedMessage"

        // Open WhatsApp
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)).apply {
            // Try to open in WhatsApp app first, fall back to browser
            setPackage("com.whatsapp")
            // If WhatsApp is not installed, this will fail and we'll catch it
        }

        // Try opening with WhatsApp app
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp app is not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
            context.startActivity(browserIntent)
        }
    }

    fun generateOrderDoneMessage(
        customer: CustomerEntity,
        order: OrderEntity,
        businessName: String = "WAW LAUNDRY"
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

        val message = StringBuilder()
        message.append("Halo Kak ${customer.name}! 🙌\n\n")
        message.append("Kami dari *$businessName* mau menginformasikan bahwa:\n\n")
        message.append("📦 *ORDER NO: ${order.orderNumber}*\n")
        message.append("✅ Cucian anda SUDAH SELESAI!\n\n")

        when (order.deliveryType) {
            DeliveryType.SELF_PICKUP -> {
                message.append("🏠 *Silakan ambil di toko ya*\n")
                message.append("   Kami sudah menunggu kehadiran anda 😊\n\n")
            }
            DeliveryType.DELIVERED -> {
                message.append("🚚 *Pesanan akan segera diantarkan*\n")
                message.append("   Kurir kami sedang menuju alamat anda\n\n")
                if (!order.notes.isNullOrBlank()) {
                    message.append("📍 Alamat: ${order.notes}\n\n")
                }
            }
        }

        // Payment reminder for unpaid or partial payment
        val remainingPayment = when (order.paymentStatus) {
            PaymentStatus.PAID -> 0.0
            PaymentStatus.UNPAID -> order.totalPrice
            PaymentStatus.PARTIAL -> order.totalPrice - order.downPayment
        }

        if (remainingPayment > 0) {
            message.append("💰 *Sisa Pembayaran: ${CurrencyFormatter.format(remainingPayment)}*\n")
            message.append("   Mohon disiapkan ya saat ${if (order.deliveryType == DeliveryType.SELF_PICKUP) "pengambilan" else "pengantaran"}\n\n")
        }

        message.append("Terima kasih sudah mempercayakan cucian anda kepada kami! 💙\n\n")
        message.append("_Mohon bales WA ini jika sudah menerima pesan ini_")

        return message.toString()
    }

    fun generateOrderWashingMessage(
        customer: CustomerEntity,
        order: OrderEntity,
        businessName: String = "WAW LAUNDRY"
    ): String {
        val message = StringBuilder()
        message.append("Halo Kak ${customer.name}! 👋\n\n")
        message.append("Dari *$businessName* mo info:\n\n")
        message.append("📦 *ORDER NO: ${order.orderNumber}*\n")
        message.append("🧺 Status: *SEDANG DIPROSES*\n")
        message.append("   Cucian anda sedang dicuci/disetrika ya 🧼\n\n")
        message.append("Kami akan kabari lagi kalau sudah selesai ya! 😊\n\n")
        message.append("Terima kasih! 💙")

        return message.toString()
    }
}
