package net.rcdevgames.wawlaundry.ui.cashier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.domain.repository.OrderRepository
import java.util.Calendar
import javax.inject.Inject

data class CashierHomeState(
    val activeOrdersCount: Int = 0,
    val todayIncome: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CashierHomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CashierHomeState())
    val state: StateFlow<CashierHomeState> = _state.asStateFlow()

    init {
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                // Calculate active orders (Not PICKED_UP)
                val activeCount = orders.count { it.orderStatus != OrderStatus.PICKED_UP }
                
                // Calculate today's income
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todayOrders = orders.filter { it.entryDate >= todayStart }
                
                var income = 0.0
                todayOrders.forEach { order ->
                    if (order.paymentStatus == PaymentStatus.PAID) {
                        income += order.totalPrice - order.discountAmount
                    } else if (order.paymentStatus == PaymentStatus.PARTIAL) {
                        income += order.downPayment
                    }
                }

                _state.update {
                    it.copy(
                        activeOrdersCount = activeCount,
                        todayIncome = income,
                        isLoading = false
                    )
                }
            }
        }
    }
}
