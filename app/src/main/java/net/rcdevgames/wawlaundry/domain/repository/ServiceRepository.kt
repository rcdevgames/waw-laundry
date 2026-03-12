package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.ServiceDao
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao
) {
    fun getAllServices(): Flow<List<ServiceEntity>> = serviceDao.getAllServices()

    suspend fun getServiceById(id: String): ServiceEntity? = serviceDao.getServiceById(id)

    suspend fun saveService(service: ServiceEntity) {
        serviceDao.insertOrUpdateService(service)
    }

    suspend fun deleteService(id: String) {
        serviceDao.softDeleteService(id)
    }

    suspend fun getUnsyncedServices(): List<ServiceEntity> = serviceDao.getUnsyncedServices()

    suspend fun markAsSynced(id: String) {
        serviceDao.markAsSynced(id)
    }
}
