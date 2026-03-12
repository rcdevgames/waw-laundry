package net.rcdevgames.wawlaundry.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.rcdevgames.wawlaundry.R
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.DeliveryType
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPdfGenerator {

    data class ReceiptData(
        val businessName: String = "WAW LAUNDRY",
        val businessPhone: String = "",
        val businessAddress: String = "",
        val order: OrderEntity,
        val details: List<OrderDetailEntity>,
        val customer: CustomerEntity,
        val footerText: String = "Terima Kasih!"
    )

    suspend fun generateReceiptPdf(
        context: Context,
        data: ReceiptData
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paint setup
            val paintNormal = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }

            val paintBold = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }

            val paintLarge = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }

            val currencyFormat = CurrencyFormatter.getNumberFormat()
            val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.forLanguageTag("id-ID"))

            var yPosition = 30f
            val leftMargin = 20f
            val rightMargin = 280f
            val lineHeight = 18f

            // Business Header
            canvas.drawText(
                data.businessName,
                (canvas.width / 2 - paintBold.measureText(data.businessName) / 2),
                yPosition,
                paintLarge
            )
            yPosition += 25f

            if (data.businessPhone.isNotBlank()) {
                canvas.drawText(
                    "WA: ${data.businessPhone}",
                    (canvas.width / 2 - paintNormal.measureText("WA: ${data.businessPhone}") / 2),
                    yPosition,
                    paintNormal
                )
                yPosition += lineHeight
            }

            // Divider
            yPosition += 5f
            drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
            yPosition += lineHeight

            // Order Info
            canvas.drawText("No: ${data.order.orderNumber}", leftMargin, yPosition, paintBold)
            yPosition += lineHeight
            canvas.drawText("Tgl: ${dateFormat.format(Date(data.order.entryDate))}", leftMargin, yPosition, paintNormal)
            yPosition += lineHeight + 5f

            drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
            yPosition += lineHeight

            // Customer Info
            canvas.drawText("Pelanggan:", leftMargin, yPosition, paintBold)
            yPosition += lineHeight
            canvas.drawText(data.customer.name, leftMargin + 5f, yPosition, paintNormal)
            yPosition += lineHeight
            if (data.customer.phone.isNotBlank()) {
                canvas.drawText("WA: ${data.customer.phone}", leftMargin + 5f, yPosition, paintNormal)
                yPosition += lineHeight
            }
            yPosition += 5f

            drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
            yPosition += lineHeight

            // Delivery Type
            val deliveryText = when (data.order.deliveryType) {
                DeliveryType.SELF_PICKUP -> "[AMBIL SENDIRI]"
                DeliveryType.DELIVERED -> "[DIANTAR]"
            }
            canvas.drawText(
                deliveryText,
                (canvas.width / 2 - paintBold.measureText(deliveryText) / 2),
                yPosition,
                paintBold
            )
            yPosition += lineHeight + 5f

            // Items
            data.details.forEach { detail ->
                val qtyText = "${detail.qty}x"
                val priceText = currencyFormat.format(detail.priceSnapshot)

                canvas.drawText(qtyText, leftMargin, yPosition, paintNormal)
                canvas.drawText("Service", leftMargin + 40f, yPosition, paintNormal)
                canvas.drawText(priceText, rightMargin - paintNormal.measureText(priceText), yPosition, paintNormal)
                yPosition += lineHeight
            }

            yPosition += 5f
            drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
            yPosition += lineHeight

            // Totals
            val servicesTotal = data.order.totalPrice + data.order.discountAmount

            if (data.order.discountAmount > 0) {
                canvas.drawText("Subtotal:", leftMargin, yPosition, paintNormal)
                canvas.drawText(
                    currencyFormat.format(servicesTotal),
                    rightMargin - paintNormal.measureText(currencyFormat.format(servicesTotal)),
                    yPosition,
                    paintNormal
                )
                yPosition += lineHeight

                canvas.drawText("Diskon:", leftMargin, yPosition, paintNormal)
                canvas.drawText(
                    "-${currencyFormat.format(data.order.discountAmount)}",
                    rightMargin - paintNormal.measureText("-${currencyFormat.format(data.order.discountAmount)}"),
                    yPosition,
                    paintNormal
                )
                yPosition += lineHeight
            }

            // Calculate delivery fee
            val deliveryFee = if (data.order.deliveryType == DeliveryType.DELIVERED) {
                data.order.totalPrice - (servicesTotal - data.order.discountAmount)
            } else {
                0.0
            }

            if (deliveryFee > 0) {
                canvas.drawText("Ongkir:", leftMargin, yPosition, paintNormal)
                canvas.drawText(
                    currencyFormat.format(deliveryFee),
                    rightMargin - paintNormal.measureText(currencyFormat.format(deliveryFee)),
                    yPosition,
                    paintNormal
                )
                yPosition += lineHeight
            }

            yPosition += 5f
            canvas.drawText("TOTAL:", leftMargin, yPosition, paintBold)
            canvas.drawText(
                currencyFormat.format(data.order.totalPrice),
                rightMargin - paintBold.measureText(currencyFormat.format(data.order.totalPrice)),
                yPosition,
                paintBold
            )
            yPosition += lineHeight + 5f

            // Payment Status
            val paymentStatusText = when (data.order.paymentStatus) {
                PaymentStatus.PAID -> "LUNAS"
                PaymentStatus.PARTIAL -> "DP: ${currencyFormat.format(data.order.downPayment)}"
                PaymentStatus.UNPAID -> "BELUM BAYAR"
            }
            canvas.drawText("Status: $paymentStatusText", leftMargin, yPosition, paintNormal)
            yPosition += lineHeight

            // Notes
            if (!data.order.notes.isNullOrBlank()) {
                yPosition += 5f
                drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
                yPosition += lineHeight
                canvas.drawText("Catatan/Alamat:", leftMargin, yPosition, paintBold)
                yPosition += lineHeight

                // Word wrap for notes
                val maxLineWidth = 260f
                val words = data.order.notes.split(" ")
                var currentLine = ""

                words.forEach { word ->
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (paintNormal.measureText(testLine) <= maxLineWidth) {
                        currentLine = testLine
                    } else {
                        canvas.drawText(currentLine, leftMargin + 5f, yPosition, paintNormal)
                        yPosition += lineHeight
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, leftMargin + 5f, yPosition, paintNormal)
                    yPosition += lineHeight
                }
            }

            // Footer
            yPosition += 15f
            drawDivider(canvas, 10f, yPosition, 290f, yPosition, paintNormal)
            yPosition += lineHeight + 10f

            canvas.drawText(
                data.footerText,
                (canvas.width / 2 - paintNormal.measureText(data.footerText) / 2),
                yPosition,
                paintNormal
            )

            pdfDocument.finishPage(page)

            // Save to file (use app-specific storage - no permission needed)
            val fileName = "RECEIPT_${data.order.orderNumber}_${System.currentTimeMillis()}.pdf"
            // Use app-specific external files directory (works without permissions)
            val appDir = context.getExternalFilesDir("receipts")
            if (appDir != null && !appDir.exists()) {
                appDir.mkdirs()
            }
            val file = if (appDir != null) {
                File(appDir, fileName)
            } else {
                // Fallback to cache directory
                File(context.cacheDir, fileName)
            }

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawDivider(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        paint: Paint
    ) {
        paint.strokeWidth = 1f
        paint.color = Color.GRAY
        canvas.drawLine(x1, y1, x2, y2, paint)
        paint.color = Color.BLACK
        paint.strokeWidth = 0f
    }
}
