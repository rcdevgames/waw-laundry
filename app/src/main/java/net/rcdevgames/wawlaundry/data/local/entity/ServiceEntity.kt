package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String,
    val price: Double,
    val categoryId: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
