package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.ProfileDao
import net.rcdevgames.wawlaundry.data.local.entity.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {

    fun getProfile(): Flow<ProfileEntity?> = profileDao.getProfile()

    suspend fun saveProfile(profile: ProfileEntity) {
        profileDao.insertOrUpdateProfile(profile)
    }

    suspend fun markProfileAsSynced(id: String) {
        profileDao.markAsSynced(id)
    }
}
