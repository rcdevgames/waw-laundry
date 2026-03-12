package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id AND isDeleted = 0")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone AND isDeleted = 0 LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteCustomer(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET totalOrders = totalOrders + 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementOrderCount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<CustomerEntity>

    @Query("UPDATE customers SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
