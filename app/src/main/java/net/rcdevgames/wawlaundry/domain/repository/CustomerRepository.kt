package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.CustomerDao
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {

    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun getCustomerById(id: String): CustomerEntity? = customerDao.getCustomerById(id)
    
    suspend fun getCustomerByPhone(phone: String): CustomerEntity? = customerDao.getCustomerByPhone(phone)

    suspend fun saveCustomer(customer: CustomerEntity) {
        customerDao.insertOrUpdateCustomer(customer)
    }

    suspend fun deleteCustomer(id: String) {
        customerDao.softDeleteCustomer(id)
    }

    suspend fun getUnsyncedCustomers(): List<CustomerEntity> = customerDao.getUnsyncedCustomers()

    suspend fun markAsSynced(id: String) {
        customerDao.markAsSynced(id)
    }

    suspend fun incrementOrderCount(id: String) {
        customerDao.incrementOrderCount(id)
    }
}
