package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id AND isDeleted = 0")
    suspend fun getServiceById(id: String): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateService(service: ServiceEntity)

    @Query("UPDATE services SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteService(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM services WHERE isSynced = 0")
    suspend fun getUnsyncedServices(): List<ServiceEntity>

    @Query("UPDATE services SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
