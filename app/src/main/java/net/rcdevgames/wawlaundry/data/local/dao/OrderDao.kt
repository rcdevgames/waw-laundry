package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE isDeleted = 0 ORDER BY entryDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus = :status AND isDeleted = 0 ORDER BY estimatedCompletionDate ASC, entryDate ASC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id AND isDeleted = 0")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerId = :customerId AND isDeleted = 0 ORDER BY entryDate DESC")
    fun getOrdersByCustomerId(customerId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrder(order: OrderEntity)

    @Query("UPDATE orders SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteOrder(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET orderStatus = :status, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: OrderStatus, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("UPDATE orders SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    // Optimized queries for reports - filter at DB level instead of client-side
    @Query("SELECT * FROM orders WHERE entryDate BETWEEN :startDate AND :endDate AND isDeleted = 0")
    suspend fun getOrdersByDateRange(startDate: Long, endDate: Long): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE entryDate BETWEEN :startDate AND :endDate AND isDeleted = 0")
    fun getOrdersByDateRangeFlow(startDate: Long, endDate: Long): Flow<List<OrderEntity>>

    @Query("SELECT SUM(totalPrice - discountAmount) FROM orders WHERE entryDate BETWEEN :startDate AND :endDate AND isDeleted = 0 AND paymentStatus = 'PAID'")
    suspend fun getPaidIncomeBetween(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(downPayment) FROM orders WHERE entryDate BETWEEN :startDate AND :endDate AND isDeleted = 0 AND paymentStatus = 'PARTIAL'")
    suspend fun getPartialIncomeBetween(startDate: Long, endDate: Long): Double?

    @Query("SELECT COUNT(*) FROM orders WHERE entryDate BETWEEN :startDate AND :endDate AND isDeleted = 0")
    suspend fun getOrderCountBetween(startDate: Long, endDate: Long): Int
}
