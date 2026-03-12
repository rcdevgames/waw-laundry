package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_details")
data class OrderDetailEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val serviceId: String,
    val qty: Float,
    val priceSnapshot: Double,
    val notes: String?,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
