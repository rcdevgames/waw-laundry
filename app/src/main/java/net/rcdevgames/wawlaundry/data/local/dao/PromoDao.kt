package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.PromoEntity

@Dao
interface PromoDao {
    @Query("SELECT * FROM promos WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllPromos(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos WHERE isActive = 1 AND isDeleted = 0 AND startDate <= :currentDate AND endDate >= :currentDate ORDER BY value DESC")
    fun getActivePromos(currentDate: Long): Flow<List<PromoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromo(promo: PromoEntity)

    @Query("UPDATE promos SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeletePromo(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM promos WHERE isSynced = 0")
    suspend fun getUnsyncedPromos(): List<PromoEntity>

    @Query("UPDATE promos SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
