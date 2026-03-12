package net.rcdevgames.wawlaundry.ui.owner.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerReportsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormat = CurrencyFormatter

    // Handle CSV file sharing
    LaunchedEffect(state.csvFilePath) {
        state.csvFilePath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists()) {
                    // Use FileProvider to get content URI (works on all Android versions)
                    val authority = "${context.packageName}.fileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, file)

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "Laporan keuangan WAW LAUNDRY")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan CSV"))
                    viewModel.clearCsvPath()
                } else {
                    snackbarHostState.showSnackbar("File CSV tidak ditemukan")
                    viewModel.clearCsvPath()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Gagal membagikan CSV: ${e.message}")
                viewModel.clearCsvPath()
            }
        }
    }

    // Period selection dialog
    var showPeriodDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan") },
                actions = {
                    IconButton(onClick = { showPeriodDialog = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Filter Periode")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.exportToCSV()
                                .onSuccess {
                                    snackbarHostState.showSnackbar("CSV berhasil dibuat, silakan pilih aplikasi untuk berbagi")
                                }
                                .onFailure { e ->
                                    snackbarHostState.showSnackbar("Gagal export: ${e.message}")
                                }
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period indicator
                PeriodSelector(
                    selectedPeriod = state.selectedPeriod,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onClick = { showPeriodDialog = true }
                )

                // Net Profit Card
                val profitColor = if (state.netProfit >= 0) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                Card(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Laba Bersih (Net Profit)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currencyFormat.format(state.netProfit),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = profitColor
                        )
                    }
                }

                // Income & Expense Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReportCard(
                        title = "Pemasukan",
                        amount = currencyFormat.format(state.totalIncome),
                        icon = Icons.Default.Payments,
                        iconTint = Color(0xFF388E3C),
                        modifier = Modifier.weight(1f)
                    )
                    ReportCard(
                        title = "Pengeluaran",
                        amount = currencyFormat.format(state.totalExpense),
                        icon = Icons.Default.MoneyOff,
                        iconTint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Orders count
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Total Pesanan", style = MaterialTheme.typography.bodyMedium)
                                Text(getPeriodText(state.selectedPeriod), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = "${state.activeOrdersCount} Nota",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Month Comparison (if available)
                state.comparison?.let { comp ->
                    ComparisonCard(
                        comparison = comp,
                        currencyFormat = currencyFormat.getNumberFormat()
                    )
                }

                // Top Customers
                if (state.topCustomers.isNotEmpty()) {
                    TopCustomersCard(
                        customers = state.topCustomers,
                        currencyFormat = currencyFormat.getNumberFormat()
                    )
                }

                // Service Performance
                if (state.servicePerformance.isNotEmpty()) {
                    ServicePerformanceCard(
                        services = state.servicePerformance,
                        currencyFormat = currencyFormat.getNumberFormat()
                    )
                }
            }
        }
    }

    if (showPeriodDialog) {
        PeriodSelectionDialog(
            selectedPeriod = state.selectedPeriod,
            onDismiss = { showPeriodDialog = false },
            onPeriodSelected = { period ->
                viewModel.setPeriod(period)
                showPeriodDialog = false
            }
        )
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    startDate: Long,
    endDate: Long,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", java.util.Locale.forLanguageTag("id-ID"))

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    getPeriodText(selectedPeriod),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PeriodSelectionDialog(
    selectedPeriod: ReportPeriod,
    onDismiss: () -> Unit,
    onPeriodSelected: (ReportPeriod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Periode") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(getPeriodText(period)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun ComparisonCard(
    comparison: MonthComparison,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "vs Bulan Lalu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Income comparison
            ComparisonRow(
                label = "Pemasukan",
                current = currencyFormat.format(comparison.currentMonthIncome),
                previous = currencyFormat.format(comparison.previousMonthIncome),
                growth = comparison.incomeGrowthPercent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Profit comparison
            ComparisonRow(
                label = "Laba Bersih",
                current = currencyFormat.format(comparison.currentMonthProfit),
                previous = currencyFormat.format(comparison.previousMonthProfit),
                growth = comparison.profitGrowthPercent
            )
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    current: String,
    previous: String,
    growth: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(previous, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("→", style = MaterialTheme.typography.bodySmall)
            Text(current, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .background(
                        if (growth >= 0) Color(0xFF388E3C).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "${if (growth >= 0) "+" else ""}${String.format("%.1f", growth)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (growth >= 0) Color(0xFF388E3C) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TopCustomersCard(
    customers: List<TopCustomer>,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Top 5 Pelanggan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            customers.take(5).forEachIndexed { index, tc ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tc.customer.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${tc.orderCount} order",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        currencyFormat.format(tc.totalSpent),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ServicePerformanceCard(
    services: List<ServicePerformance>,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Checkroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Performa Layanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            services.take(5).forEachIndexed { index, sp ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            sp.service.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${sp.orderCount}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            currencyFormat.format(sp.totalRevenue),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Progress bar
                    val maxValue = services.maxOfOrNull { it.totalRevenue } ?: 1.0
                    LinearProgressIndicator(
                        progress = { (sp.totalRevenue / maxValue).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

fun getPeriodText(period: ReportPeriod): String {
    return when (period) {
        ReportPeriod.TODAY -> "Hari Ini"
        ReportPeriod.THIS_WEEK -> "Minggu Ini"
        ReportPeriod.THIS_MONTH -> "Bulan Ini"
        ReportPeriod.LAST_MONTH -> "Bulan Lalu"
        ReportPeriod.CUSTOM -> "Custom"
    }
}

@Composable
fun ReportCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
