package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE isDeleted = 0 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity)
    
    @Query("SELECT * FROM profiles WHERE isSynced = 0")
    suspend fun getUnsyncedProfiles(): List<ProfileEntity>

    @Query("UPDATE profiles SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
