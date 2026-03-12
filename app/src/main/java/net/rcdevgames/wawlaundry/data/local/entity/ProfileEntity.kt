package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val businessName: String,
    val address: String,
    val phone: String,
    val headerText: String,
    val footerText: String,
    val logoPath: String?,
    val themeColor: String,
    val isDarkMode: Boolean,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
