package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PromoType {
    PERCENTAGE, NOMINAL
}

@Entity(tableName = "promos")
data class PromoEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val title: String,
    val promoType: PromoType,
    val value: Double,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
