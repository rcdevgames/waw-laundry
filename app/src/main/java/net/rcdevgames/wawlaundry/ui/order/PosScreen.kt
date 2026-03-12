package net.rcdevgames.wawlaundry.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.rcdevgames.wawlaundry.data.local.entity.CustomerEntity
import net.rcdevgames.wawlaundry.data.local.entity.DeliveryType
import net.rcdevgames.wawlaundry.data.local.entity.PaymentMethod
import net.rcdevgames.wawlaundry.data.local.entity.PaymentStatus
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import net.rcdevgames.wawlaundry.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onNavigateBack: () -> Unit,
    onFinishActivity: () -> Unit,
    viewModel: NewOrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormat = CurrencyFormatter
    var showPromoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isOrderSaved) {
        if (state.isOrderSaved) {
            onFinishActivity()
        }
    }

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
                title = { 
                    Column {
                        Text("Transaksi Baru")
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
        },
        bottomBar = {
            val total = viewModel.calculateTotal()
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Tagihan:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = currencyFormat.format(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            if (state.selectedCustomer == null) {
                                viewModel.toggleCustomerSheet(true)
                            } else {
                                viewModel.toggleCheckoutSheet(true)
                            }
                        },
                        enabled = state.selectedServices.isNotEmpty()
                    ) {
                        Text(if (state.selectedCustomer == null) "Pilih Pelanggan" else "BAYAR")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Customer Header Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { viewModel.toggleCustomerSheet(true) },
                colors = CardDefaults.cardColors(
                    containerColor = if (state.selectedCustomer == null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person, 
                            contentDescription = null,
                            tint = if (state.selectedCustomer == null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = state.selectedCustomer?.name ?: "Pilih Pelanggan Dulu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.selectedCustomer == null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (state.selectedCustomer != null) {
                                Text(
                                    text = state.selectedCustomer!!.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown, 
                        contentDescription = "Ganti Pelanggan",
                        tint = if (state.selectedCustomer == null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Services List
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.services.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada layanan tersedia.\nTambahkan di menu Owner.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.services) { service ->
                        val currentQty = state.selectedServices[service] ?: 0f
                        PosServiceItem(
                            service = service,
                            qty = currentQty,
                            onAdd = { viewModel.addService(service) },
                            onDecrease = { viewModel.decreaseService(service) }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (state.showCustomerSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleCustomerSheet(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            CustomerSheetContent(viewModel = viewModel)
        }
    }

    if (state.showCheckoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleCheckoutSheet(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CheckoutSheetContent(
                viewModel = viewModel,
                onShowPromoDialog = { showPromoDialog = true }
            )
        }
    }

    if (showPromoDialog) {
        PromoSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showPromoDialog = false }
        )
    }

    if (state.showNewCustomerDialog) {
        NewCustomerDialog(viewModel = viewModel)
    }
}

@Composable
fun PosServiceItem(
    service: ServiceEntity,
    qty: Float,
    onAdd: () -> Unit,
    onDecrease: () -> Unit
) {
    val currencyFormat = CurrencyFormatter
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currencyFormat.format(service.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (qty > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Text(
                        text = if (qty % 1 == 0f) qty.toInt().toString() else qty.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            } else {
                OutlinedButton(onClick = onAdd, modifier = Modifier.height(36.dp)) {
                    Text("Tambah")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSheetContent(viewModel: NewOrderViewModel) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pilih Pelanggan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.toggleNewCustomerDialog(true) }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Pelanggan Baru", tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cari nama atau nomor HP...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )

        val filteredCustomers = state.customers.filter {
            it.name.contains(state.searchQuery, ignoreCase = true) || 
            it.phone.contains(state.searchQuery, ignoreCase = true)
        }

        if (filteredCustomers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Pelanggan tidak ditemukan")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(filteredCustomers) { customer ->
                    val isSelected = state.selectedCustomer?.id == customer.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { 
                                viewModel.selectCustomer(customer)
                                viewModel.toggleCustomerSheet(false)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = customer.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutSheetContent(
    viewModel: NewOrderViewModel,
    onShowPromoDialog: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = CurrencyFormatter

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text("Konfirmasi Pembayaran", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Subtotals Card
        val subTotal = viewModel.calculateSubTotal()
        val discount = viewModel.calculateDiscount(subTotal)
        val deliveryFee = viewModel.getDeliveryFee()

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(subTotal), style = MaterialTheme.typography.bodyLarge)
                }
                if (discount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon Promo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        Text("-${currencyFormat.format(discount)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
                if (deliveryFee > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ongkos Kirim", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currencyFormat.format(deliveryFee), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Bersih", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormat.format(viewModel.calculateTotal()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedCard(
            onClick = onShowPromoDialog,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                if (state.selectedPromo != null) {
                    Column {
                        Text("Promo Aktif", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(state.selectedPromo!!.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Gunakan Promo / Diskon", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Pembayaran
        Text("Status Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CheckoutOptionButton(
                text = "Belum Bayar",
                selected = state.paymentStatus == PaymentStatus.UNPAID,
                onClick = { viewModel.setPaymentStatus(PaymentStatus.UNPAID) },
                modifier = Modifier.weight(1f)
            )
            CheckoutOptionButton(
                text = "DP",
                selected = state.paymentStatus == PaymentStatus.PARTIAL,
                onClick = { viewModel.setPaymentStatus(PaymentStatus.PARTIAL) },
                modifier = Modifier.weight(1f)
            )
            CheckoutOptionButton(
                text = "Lunas",
                selected = state.paymentStatus == PaymentStatus.PAID,
                onClick = { viewModel.setPaymentStatus(PaymentStatus.PAID) },
                modifier = Modifier.weight(1f)
            )
        }

        if (state.paymentStatus == PaymentStatus.PARTIAL) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.downPaymentText,
                onValueChange = viewModel::onDownPaymentChange,
                label = { Text("Nominal DP (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (state.paymentStatus != PaymentStatus.UNPAID) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckoutOptionButton(
                    text = "Tunai",
                    selected = state.paymentMethod == PaymentMethod.CASH,
                    onClick = { viewModel.setPaymentMethod(PaymentMethod.CASH) },
                    modifier = Modifier.weight(1f)
                )
                CheckoutOptionButton(
                    text = "Transfer",
                    selected = state.paymentMethod == PaymentMethod.TRANSFER,
                    onClick = { viewModel.setPaymentMethod(PaymentMethod.TRANSFER) },
                    modifier = Modifier.weight(1f)
                )
                CheckoutOptionButton(
                    text = "QRIS",
                    selected = state.paymentMethod == PaymentMethod.QRIS,
                    onClick = { viewModel.setPaymentMethod(PaymentMethod.QRIS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pengambilan & Catatan
        Text("Opsi Pengambilan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CheckoutOptionButton(
                text = "Ambil Sendiri",
                selected = state.deliveryType == DeliveryType.SELF_PICKUP,
                onClick = { viewModel.setDeliveryType(DeliveryType.SELF_PICKUP) },
                modifier = Modifier.weight(1f)
            )
            CheckoutOptionButton(
                text = "Diantar (Delivery)",
                selected = state.deliveryType == DeliveryType.DELIVERED,
                onClick = { viewModel.setDeliveryType(DeliveryType.DELIVERED) },
                modifier = Modifier.weight(1f)
            )
        }

        // Delivery Fee Input (only show when delivered)
        if (state.deliveryType == DeliveryType.DELIVERED) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.deliveryFeeText,
                onValueChange = viewModel::onDeliveryFeeChange,
                label = { Text("Ongkos Kirim (Rp)") },
                leadingIcon = { Text("Rp", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            label = {
                val labelText = if (state.deliveryType == DeliveryType.DELIVERED) {
                    "Alamat Pengiriman *"
                } else {
                    "Catatan Pesanan (opsional)"
                }
                Text(
                    labelText,
                    color = if (state.deliveryType == DeliveryType.DELIVERED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            placeholder = if (state.deliveryType == DeliveryType.DELIVERED) {
                { Text("Masukkan alamat lengkap pengiriman...", style = MaterialTheme.typography.bodySmall) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            minLines = if (state.deliveryType == DeliveryType.DELIVERED) 4 else 2,
            maxLines = 6,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.processOrder() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("PROSES TRANSAKSI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CheckoutOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PromoSelectionDialog(
    viewModel: NewOrderViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = CurrencyFormatter

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Promo / Diskon") },
        text = {
            if (state.promos.isEmpty()) {
                Text("Tidak ada promo aktif saat ini.")
            } else {
                LazyColumn {
                    items(state.promos) { promo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPromo(promo)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "${promo.title} - ${if (promo.promoType == net.rcdevgames.wawlaundry.data.local.entity.PromoType.PERCENTAGE) "${promo.value}%" else currencyFormat.format(promo.value)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    item {
                        TextButton(
                            onClick = { 
                                viewModel.selectPromo(null)
                                onDismiss() 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hapus Promo (Tanpa Promo)", color = MaterialTheme.colorScheme.error)
                        }
                    }
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
fun NewCustomerDialog(viewModel: NewOrderViewModel) {
    val state by viewModel.state.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.toggleNewCustomerDialog(false) },
        title = { Text("Pelanggan Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.newCustomerName,
                    onValueChange = viewModel::onNewCustomerNameChange,
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.newCustomerPhone,
                    onValueChange = viewModel::onNewCustomerPhoneChange,
                    label = { Text("No. HP / WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::saveNewCustomer) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleNewCustomerDialog(false) }) {
                Text("Batal")
            }
        }
    )
}
