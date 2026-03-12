package net.rcdevgames.wawlaundry.ui.queue

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderEntity
import net.rcdevgames.wawlaundry.data.local.entity.OrderStatus
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderQueueScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderQueueViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf(OrderStatus.QUEUE, OrderStatus.WASHING, OrderStatus.DONE, OrderStatus.PICKED_UP)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle PDF sharing
    LaunchedEffect(state.pdfPath) {
        state.pdfPath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists()) {
                    // Use FileProvider to get content URI (works on all Android versions)
                    val authority = "${context.packageName}.fileprovider"
                    val uri = FileProvider.getUriForFile(context, authority, file)

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "Struk laundry dari WAW LAUNDRY")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Struk PDF"))
                    viewModel.clearPdfPath()
                } else {
                    viewModel.showError("File PDF tidak ditemukan")
                    viewModel.clearPdfPath()
                }
            } catch (e: Exception) {
                viewModel.showError("Gagal membagikan PDF: ${e.message}")
                viewModel.clearPdfPath()
            }
        }
    }

    // Show error messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Antrian Cucian") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(state.selectedTab),
                edgePadding = 8.dp
            ) {
                tabs.forEach { tab ->
                    val tabName = when (tab) {
                        OrderStatus.QUEUE -> "Antrian Baru"
                        OrderStatus.WASHING -> "Dicuci / Setrika"
                        OrderStatus.DONE -> "Selesai (Siap Ambil)"
                        OrderStatus.PICKED_UP -> "Sudah Diambil"
                    }
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tabName, maxLines = 1) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Tidak ada order di status ini.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredOrders) { order ->
                        val customer = state.customers[order.customerId]
                        QueueCard(
                            order = order,
                            customer = customer,
                            onUpdateStatus = { targetStatus ->
                                viewModel.updateOrderStatus(order, targetStatus)
                            },
                            onSendWhatsApp = { messageType ->
                                viewModel.sendWhatsAppNotification(order, messageType)
                            },
                            onSharePdf = {
                                coroutineScope.launch {
                                    viewModel.generateAndShareReceipt(order)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueCard(
    order: OrderEntity,
    customer: CustomerEntity?,
    onUpdateStatus: (OrderStatus) -> Unit,
    onSendWhatsApp: (WhatsAppMessageType) -> Unit,
    onSharePdf: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM HH:mm", java.util.Locale.forLanguageTag("id-ID"))
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (customer != null) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = dateFormat.format(Date(order.entryDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(when(order.paymentStatus) {
                            PaymentStatus.UNPAID -> "Belum Bayar"
                            PaymentStatus.PARTIAL -> "DP"
                            PaymentStatus.PAID -> "Lunas"
                        })
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (order.paymentStatus == PaymentStatus.PAID) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // WhatsApp Button
                IconButton(
                    onClick = { showWhatsAppDialog = true },
                    modifier = Modifier.size(40.dp),
                    enabled = customer?.phone?.isNotBlank() == true
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Kirim WA",
                        tint = if (customer?.phone?.isNotBlank() == true)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // PDF Share Button
                IconButton(
                    onClick = onSharePdf,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Share PDF",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Status Change Button
                when (order.orderStatus) {
                    OrderStatus.QUEUE -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.WASHING) },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Mulai", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OrderStatus.WASHING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DONE) },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Selesai", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OrderStatus.DONE -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PICKED_UP) },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Ambil", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OrderStatus.PICKED_UP -> {
                        AssistChip(
                            onClick = { },
                            label = { Text("Selesai", style = MaterialTheme.typography.bodySmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    }

    // WhatsApp Message Selection Dialog
    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Kirim WhatsApp") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pilih pesan yang akan dikirim:")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (order.orderStatus == OrderStatus.WASHING) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onSendWhatsApp(WhatsAppMessageType.WASHING)
                                showWhatsAppDialog = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "🧺 Status: Sedang Diproses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Informasikan bahwa cucian sedang dicuci/disetrika",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (order.orderStatus == OrderStatus.DONE || order.orderStatus == OrderStatus.PICKED_UP) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onSendWhatsApp(WhatsAppMessageType.DONE)
                                showWhatsAppDialog = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "✅ Status: Selesai (Siap Ambil)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Informasikan cucian sudah selesai + sisa pembayaran (jika ada)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
