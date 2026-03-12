package net.rcdevgames.wawlaundry.ui.owner.printer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPrinterScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerPrinterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Permissions logic
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    }

    var hasPermissions by remember {
        mutableStateOf(bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            viewModel.loadPrinters()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(bluetoothPermissions.toTypedArray())
        } else {
            viewModel.loadPrinters()
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Printer default berhasil disimpan")
            viewModel.clearSavedStatus()
        }
    }

    LaunchedEffect(state.isTestPrintSuccess) {
        if (state.isTestPrintSuccess) {
            snackbarHostState.showSnackbar("Test print berhasil dikirim ke printer")
            viewModel.clearTestPrintSuccessStatus()
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
                title = { Text("Pengaturan Printer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (hasPermissions || state.printerType == "USB") {
                Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                    Button(
                        onClick = viewModel::testPrint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("TEST PRINT")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Printer POS Thermal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioOption(
                        text = "Bluetooth",
                        selected = state.printerType == "Bluetooth",
                        onClick = { viewModel.onPrinterTypeChange("Bluetooth") },
                        modifier = Modifier.weight(1f)
                    )
                    RadioOption(
                        text = "USB",
                        selected = state.printerType == "USB",
                        onClick = { viewModel.onPrinterTypeChange("USB") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("Kertas Printer/WhatsApp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioOption(
                        text = "58 mm",
                        selected = state.paperSize == 58,
                        onClick = { viewModel.onPaperSizeChange(58) },
                        modifier = Modifier.weight(1f),
                        outlined = false
                    )
                    RadioOption(
                        text = "80 mm",
                        selected = state.paperSize == 80,
                        onClick = { viewModel.onPaperSizeChange(80) },
                        modifier = Modifier.weight(1f),
                        outlined = false
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SwitchOption("Cetak dalam mode grafis (sesuai preview dan teks lebih kecil)", state.printGraphic, viewModel::onPrintGraphicChange)
                HorizontalDivider()
                SwitchOption("Cetak setelah transaksi", state.printAfterTransaction, viewModel::onPrintAfterTransactionChange)
                HorizontalDivider()
                SwitchOption("Cetak struk dua kali", state.printTwice, viewModel::onPrintTwiceChange)
                HorizontalDivider()
                SwitchOption("Laci Uang tersambung ke Printer", state.cashDrawer, viewModel::onCashDrawerChange)
                HorizontalDivider()
                SwitchOption("Jika hasil cetak terlalu panjang di bagian bawah, hidupkan opsi ini", state.longReceipt, viewModel::onLongReceiptChange)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            if (state.printerType == "Bluetooth") {
                if (!hasPermissions) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Akses Bluetooth Diperlukan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Aplikasi membutuhkan izin Bluetooth untuk mencari dan menghubungkan ke printer kasir. Silakan berikan izin untuk melanjutkan.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { permissionLauncher.launch(bluetoothPermissions.toTypedArray()) }) {
                                Text("Minta Izin Bluetooth")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }) {
                                Text("Buka Pengaturan HP Secara Manual")
                            }
                        }
                    }
                } else if (state.isLoading && state.pairedDevices.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Perangkat Bluetooth Dipasangkan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.loadPrinters() }, modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))) {
                                Icon(Icons.Default.Print, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (state.pairedDevices.isEmpty()) {
                            Text(
                                text = "Tidak ada perangkat Bluetooth. Pastikan Bluetooth aktif dan sudah dipasangkan di setting HP.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    items(state.pairedDevices) { device ->
                        val isSelected = device.address == state.defaultPrinterAddress
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setDefaultPrinter(device) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                @SuppressLint("MissingPermission")
                                val name = device.name ?: "Unknown Device"
                                Text(text = name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Text(text = device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        HorizontalDivider()
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Pengaturan bahasa printer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        val languages = listOf("Alfabet Standar", "Prancis, Spanyol, Italia, Portugis, Jerman", "Jepang", "Mandarin", "Rusia", "Use ESC/POS Commands")
                        languages.forEach { lang ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onPrinterLanguageChange(lang) }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = state.printerLanguage == lang,
                                    onClick = { viewModel.onPrinterLanguageChange(lang) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lang, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Not all bluetooth printer support your language. You can test them here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom button
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for bottom button
                }
            }
        }
    }
}

@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = if (outlined) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
        border = if (outlined) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SwitchOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
