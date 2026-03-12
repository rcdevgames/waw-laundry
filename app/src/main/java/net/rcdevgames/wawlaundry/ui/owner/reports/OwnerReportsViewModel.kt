package net.rcdevgames.wawlaundry.ui.owner.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderDetailEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import java.util.Date
import net.rcdevgames.wawlaundry.domain.repository.CustomerRepository
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import net.rcdevgames.wawlaundry.domain.repository.ExpenseRepository
import net.rcdevgames.wawlaundry.domain.repository.OrderRepository
import net.rcdevgames.wawlaundry.domain.repository.ServiceRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// Data classes for reports
data class TopCustomer(
    val customer: CustomerEntity,
    val orderCount: Int,
    val totalSpent: Double
)

data class ServicePerformance(
    val service: ServiceEntity,
    val orderCount: Int,
    val totalRevenue: Double
)

data class MonthComparison(
    val currentMonthIncome: Double,
    val currentMonthExpense: Double,
    val currentMonthProfit: Double,
    val previousMonthIncome: Double,
    val previousMonthExpense: Double,
    val previousMonthProfit: Double,
    val incomeGrowthPercent: Double,
    val profitGrowthPercent: Double
)

data class OwnerReportsState(
    // Date filter
    val startDate: Long = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis,
    val endDate: Long = Calendar.getInstance().timeInMillis,

    // Current month stats
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val activeOrdersCount: Int = 0,

    // Previous month comparison
    val comparison: MonthComparison? = null,

    // Top customers (limit 10)
    val topCustomers: List<TopCustomer> = emptyList(),

    // Service performance
    val servicePerformance: List<ServicePerformance> = emptyList(),

    // UI state
    val isLoading: Boolean = true,
    val selectedPeriod: ReportPeriod = ReportPeriod.THIS_MONTH,
    val csvFilePath: String? = null
)

enum class ReportPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}

@HiltViewModel
class OwnerReportsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val serviceRepository: ServiceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerReportsState())
    val state: StateFlow<OwnerReportsState> = _state.asStateFlow()

    private val currencyFormat = CurrencyFormatter

    init {
        loadReports()
    }

    fun setPeriod(period: ReportPeriod) {
        val calendar = Calendar.getInstance()

        val (newStart, newEnd) = when (period) {
            ReportPeriod.TODAY -> {
                Pair(
                    calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis,
                    calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                )
            }
            ReportPeriod.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Pair(
                    calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis,
                    Calendar.getInstance().timeInMillis
                )
            }
            ReportPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                Pair(
                    calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis,
                    Calendar.getInstance().timeInMillis
                )
            }
            ReportPeriod.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.CUSTOM -> {
                Pair(_state.value.startDate, _state.value.endDate)
            }
        }

        _state.update { it.copy(startDate = newStart, endDate = newEnd, selectedPeriod = period) }
        loadReports()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _state.update { it.copy(startDate = start, endDate = end, selectedPeriod = ReportPeriod.CUSTOM) }
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val start = _state.value.startDate
            val end = _state.value.endDate

            // Calculate previous month dates for comparison
            val cal = Calendar.getInstance()
            cal.timeInMillis = start
            cal.add(Calendar.MONTH, -1)
            val prevStart = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val prevEnd = cal.timeInMillis

            // Load orders
            orderRepository.getAllOrders().collect { orders ->
                val periodOrders = orders.filter { it.entryDate in start..end }
                val prevPeriodOrders = orders.filter { it.entryDate in prevStart..prevEnd }

                // Calculate income
                var income = 0.0
                periodOrders.forEach { order ->
                    if (order.paymentStatus == PaymentStatus.PAID) {
                        income += (order.totalPrice - order.discountAmount)
                    } else if (order.paymentStatus == PaymentStatus.PARTIAL) {
                        income += order.downPayment
                    }
                }

                var prevIncome = 0.0
                prevPeriodOrders.forEach { order ->
                    if (order.paymentStatus == PaymentStatus.PAID) {
                        prevIncome += (order.totalPrice - order.discountAmount)
                    } else if (order.paymentStatus == PaymentStatus.PARTIAL) {
                        prevIncome += order.downPayment
                    }
                }

                _state.update { current ->
                    current.copy(
                        totalIncome = income,
                        activeOrdersCount = periodOrders.size,
                        comparison = current.comparison?.copy(
                            currentMonthIncome = income,
                            previousMonthIncome = prevIncome,
                            incomeGrowthPercent = if (prevIncome > 0) ((income - prevIncome) / prevIncome * 100) else 0.0
                        )
                    )
                }
            }

            // Load expenses
            expenseRepository.getAllExpenses().collect { expenses ->
                val periodExpenses = expenses.filter { it.date in start..end }
                val prevPeriodExpenses = expenses.filter { it.date in prevStart..prevEnd }
                val totalExp = periodExpenses.sumOf { it.amount }
                val prevTotalExp = prevPeriodExpenses.sumOf { it.amount }

                _state.update { current ->
                    val profit = current.totalIncome - totalExp
                    val prevProfit = current.comparison?.previousMonthIncome ?: 0.0 - prevTotalExp

                    current.copy(
                        totalExpense = totalExp,
                        netProfit = profit,
                        comparison = MonthComparison(
                            currentMonthIncome = current.totalIncome,
                            currentMonthExpense = totalExp,
                            currentMonthProfit = profit,
                            previousMonthIncome = current.comparison?.previousMonthIncome ?: 0.0,
                            previousMonthExpense = prevTotalExp,
                            previousMonthProfit = prevProfit,
                            incomeGrowthPercent = current.comparison?.incomeGrowthPercent ?: 0.0,
                            profitGrowthPercent = if (prevProfit > 0) ((profit - prevProfit) / prevProfit * 100) else 0.0
                        )
                    )
                }
            }

            // Load top customers
            loadTopCustomers(start, end)

            // Load service performance
            loadServicePerformance(start, end)

            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadTopCustomers(start: Long, end: Long) {
        orderRepository.getAllOrders().collect { orders ->
            val periodOrders = orders.filter { it.entryDate in start..end }

            // Aggregate by customer
            val customerMap = mutableMapOf<String, TopCustomerData>()

            periodOrders.forEach { order ->
                val data = customerMap.getOrPut(order.customerId) {
                    TopCustomerData()
                }
                data.orderIds.add(order.id)
                data.totalSpent += (order.totalPrice - order.discountAmount)
            }

            // Get customer details and sort
            val allCustomers = customerRepository.getAllCustomers().first()
            val topCustomersList = customerMap.entries.mapNotNull { (customerId, data) ->
                val customer = allCustomers.find { it.id == customerId }
                customer?.let {
                    TopCustomer(
                        customer = it,
                        orderCount = data.orderIds.size,
                        totalSpent = data.totalSpent
                    )
                }
            }.sortedByDescending { it.totalSpent }.take(10)

            _state.update { it.copy(topCustomers = topCustomersList) }
        }
    }

    private suspend fun loadServicePerformance(start: Long, end: Long) {
        val periodOrders = orderRepository.getAllOrders().first()
            .filter { it.entryDate in start..end }
            .map { it.id }

        if (periodOrders.isEmpty()) {
            _state.update { it.copy(servicePerformance = emptyList()) }
            return
        }

        // Get all order details for this period
        val servicePerformanceMap = mutableMapOf<String, ServicePerformanceData>()

        periodOrders.forEach { orderId ->
            val details = orderRepository.getOrderDetails(orderId).first()
            details.forEach { detail ->
                val data = servicePerformanceMap.getOrPut(detail.serviceId) {
                    ServicePerformanceData(orderCount = 0, totalRevenue = 0.0)
                }
                data.orderCount += detail.qty.toInt()
                data.totalRevenue += (detail.priceSnapshot * detail.qty)
            }
        }

        // Get service details and sort
        val allServices = serviceRepository.getAllServices().first()
        val servicePerformanceList = servicePerformanceMap.entries.mapNotNull { (serviceId, data) ->
            val service = allServices.find { it.id == serviceId }
            service?.let {
                ServicePerformance(
                    service = it,
                    orderCount = data.orderCount,
                    totalRevenue = data.totalRevenue
                )
            }
        }.sortedByDescending { it.totalRevenue }

        _state.update { it.copy(servicePerformance = servicePerformanceList) }
    }

    fun exportToCSV(): Result<String> {
        return try {
            val state = _state.value
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))

            val csv = StringBuilder()
            csv.append("LAPORAN KEUANGAN WAW LAUNDRY\n")
            csv.append("Periode: ${dateFormat.format(Date(state.startDate))} - ${dateFormat.format(Date(state.endDate))}\n\n")

            // Summary
            csv.append("RINGKASAN\n")
            csv.append("Pemasukan,${state.totalIncome}\n")
            csv.append("Pengeluaran,${state.totalExpense}\n")
            csv.append("Laba Bersih,${state.netProfit}\n")
            csv.append("Jumlah Pesanan,${state.activeOrdersCount}\n\n")

            // Comparison if available
            state.comparison?.let { comp ->
                csv.append("PERBANDINGAN BULAN LALU\n")
                csv.append("Bulan Ini,Bulan Lalu,Pertumbuhan (%)\n")
                csv.append("Pemasukan,${comp.currentMonthIncome},${comp.previousMonthIncome},${String.format("%.2f", comp.incomeGrowthPercent)}\n")
                csv.append("Pengeluaran,${comp.currentMonthExpense},${comp.previousMonthExpense},-\n")
                csv.append("Laba Bersih,${comp.currentMonthProfit},${comp.previousMonthProfit},${String.format("%.2f", comp.profitGrowthPercent)}\n\n")
            }

            // Top customers
            if (state.topCustomers.isNotEmpty()) {
                csv.append("TOP PELANGGAN\n")
                csv.append("Nama,No HP,Jumlah Order,Total Belanja\n")
                state.topCustomers.forEach { tc ->
                    csv.append("\"${tc.customer.name}\",\"${tc.customer.phone}\",${tc.orderCount},${tc.totalSpent}\n")
                }
                csv.append("\n")
            }

            // Service performance
            if (state.servicePerformance.isNotEmpty()) {
                csv.append("PERFORMA LAYANAN\n")
                csv.append("Layanan,Jumlah Order,Pendapatan Total\n")
                state.servicePerformance.forEach { sp ->
                    csv.append("\"${sp.service.name}\",${sp.orderCount},${sp.totalRevenue}\n")
                }
            }

            // Save to app-specific storage (no permission needed)
            val fileName = "Laporan_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())}.csv"
            val appDir = context.getExternalFilesDir("reports")
            if (appDir != null && !appDir.exists()) {
                appDir.mkdirs()
            }
            val file = if (appDir != null) {
                File(appDir, fileName)
            } else {
                // Fallback to cache directory
                File(context.cacheDir, fileName)
            }

            file.writeText(csv.toString(), Charsets.UTF_8)

            _state.update { it.copy(csvFilePath = file.absolutePath) }
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearCsvPath() {
        _state.update { it.copy(csvFilePath = null) }
    }

    // Helper data classes
    private data class TopCustomerData(
        val orderIds: MutableList<String> = mutableListOf(),
        var totalSpent: Double = 0.0
    )

    private data class ServicePerformanceData(
        var orderCount: Int = 0,
        var totalRevenue: Double = 0.0
    )
}
