package net.rcdevgames.wawlaundry.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity

@Dao
interface OrderDetailDao {
    @Query("SELECT * FROM order_details WHERE orderId = :orderId AND isDeleted = 0")
    fun getDetailsByOrderId(orderId: String): Flow<List<OrderDetailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrderDetails(orderDetails: List<OrderDetailEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrderDetail(orderDetail: OrderDetailEntity)

    @Query("UPDATE order_details SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE orderId = :orderId")
    suspend fun softDeleteAllByOrderId(orderId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE order_details SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteOrderDetail(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM order_details WHERE isSynced = 0")
    suspend fun getUnsyncedOrderDetails(): List<OrderDetailEntity>

    @Query("UPDATE order_details SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
