package net.rcdevgames.wawlaundry.domain.repository

import kotlinx.coroutines.flow.Flow
import net.rcdevgames.wawlaundry.data.local.dao.OrderDao
import net.rcdevgames.wawlaundry.data.local.dao.OrderDetailDao
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val orderDetailDao: OrderDetailDao
) {

    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>> = orderDao.getOrdersByStatus(status)

    suspend fun getOrderById(id: String): OrderEntity? = orderDao.getOrderById(id)

    fun getOrderDetails(orderId: String): Flow<List<OrderDetailEntity>> = orderDetailDao.getDetailsByOrderId(orderId)

    suspend fun saveCompleteOrder(order: OrderEntity, details: List<OrderDetailEntity>) {
        orderDao.insertOrUpdateOrder(order)
        orderDetailDao.insertOrUpdateOrderDetails(details)
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    suspend fun getUnsyncedOrders(): List<OrderEntity> = orderDao.getUnsyncedOrders()

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.insertOrUpdateOrder(order)
    }

    suspend fun getUnsyncedOrderDetails(): List<OrderDetailEntity> = orderDetailDao.getUnsyncedOrderDetails()

    suspend fun updateOrderDetail(orderDetail: OrderDetailEntity) {
        orderDetailDao.insertOrUpdateOrderDetail(orderDetail)
    }

    suspend fun markOrderAsSynced(id: String) {
        orderDao.markAsSynced(id)
    }

    suspend fun markOrderDetailAsSynced(id: String) {
        orderDetailDao.markAsSynced(id)
    }
}
