package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentStatus {
    UNPAID, PARTIAL, PAID
}

enum class OrderStatus {
    QUEUE, WASHING, DONE, PICKED_UP
}

enum class PaymentMethod {
    CASH, TRANSFER, QRIS
}

enum class DeliveryType {
    SELF_PICKUP, DELIVERED
}

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["entryDate", "isDeleted"]),
        Index(value = ["orderStatus", "isDeleted"]),
        Index(value = ["customerId", "isDeleted"]),
        Index(value = ["isSynced"]),
        Index(value = ["isDeleted"])
    ]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val customerId: String,
    val promoId: String?,
    val orderNumber: String,
    val entryDate: Long,
    val estimatedCompletionDate: Long?,
    val exitDate: Long?,
    val totalPrice: Double,
    val discountAmount: Double,
    val downPayment: Double,
    val paymentStatus: PaymentStatus,
    val orderStatus: OrderStatus,
    val paymentMethod: PaymentMethod,
    val deliveryType: DeliveryType,
    val notes: String?,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
