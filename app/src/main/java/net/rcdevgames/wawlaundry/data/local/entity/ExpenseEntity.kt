package net.rcdevgames.wawlaundry.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date", "isDeleted"]),
        Index(value = ["isDeleted"]),
        Index(value = ["isSynced"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val title: String,
    val amount: Double,
    val date: Long,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)
