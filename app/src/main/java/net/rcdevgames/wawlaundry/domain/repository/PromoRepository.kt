package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.PromoDao
import net.rcdevgames.wawlaundry.data.local.entity.PromoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoRepository @Inject constructor(
    private val promoDao: PromoDao
) {

    fun getAllPromos(): Flow<List<PromoEntity>> = promoDao.getAllPromos()

    fun getActivePromos(currentDate: Long): Flow<List<PromoEntity>> = promoDao.getActivePromos(currentDate)

    suspend fun savePromo(promo: PromoEntity) {
        promoDao.insertOrUpdatePromo(promo)
    }

    suspend fun softDeletePromo(id: String, timestamp: Long = System.currentTimeMillis()) {
        promoDao.softDeletePromo(id, timestamp)
    }

    suspend fun getUnsyncedPromos(): List<PromoEntity> = promoDao.getUnsyncedPromos()

    suspend fun markAsSynced(id: String) {
        promoDao.markAsSynced(id)
    }
}
