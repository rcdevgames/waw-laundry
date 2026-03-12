package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name", "isDeleted"]),
        Index(value = ["phone", "isDeleted"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isSynced"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String,
    val phone: String,
    val address: String?,
    val totalOrders: Int = 0,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
