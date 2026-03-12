package net.rcdevgames.wawlaundry.ui.owner.data

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDataScreen(
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: OwnerDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Backup file picker launcher (save)
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.performBackup(uri)
        }
    }

    // Restore file picker launcher (open)
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onRestoreFileSelected(uri)
        }
    }

    LaunchedEffect(state.isResetSuccess) {
        if (state.isResetSuccess) {
            onResetSuccess()
        }
    }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Data & Reset") },
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Backup & Restore Lokal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                .format(java.util.Date())
                            viewModel.onBackupDataClick {
                                backupLauncher.launch("waw_laundry_backup_$timestamp.db")
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Backup",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Backup Data ke File", fontWeight = FontWeight.Bold)
                        Text("Simpan database ke file untuk cadangan lokal", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.onRestoreDataClick { restoreLauncher.launch(arrayOf("*/*")) } }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restore dari File Backup", fontWeight = FontWeight.Bold)
                        Text("Pulihkan database dari file backup (memerlukan master password)", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Zona Berbahaya (Danger Zone)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { viewModel.onResetRequest() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reset Data Lokal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Menghapus SEMUA data toko dari HP dan mengulang setup dari awal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha=0.8f))
                    }
                }
            }

            if (state.isSyncing || state.isResetting || state.isBackingUp || state.isRestoring) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    if (state.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onResetCancel,
            title = {
                Text(
                    text = "Konfirmasi Reset Database",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Tindakan ini tidak dapat dibatalkan. Semua transaksi, setting toko, pin, dan master password akan dihapus.\n\nMasukkan PIN Owner untuk melanjutkan asalkan Anda yakin.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.resetPinInput,
                        onValueChange = viewModel::onPinChange,
                        label = { Text("PIN Owner (6 Digit)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.pinError,
                        supportingText = { if (state.pinError) Text("PIN Salah!") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmReset,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("HAPUS DATABASE")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onResetCancel) {
                    Text("Batal")
                }
            }
        )
    }

    // Restore password verification dialog
    if (state.showRestoreDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onRestoreCancel,
            title = {
                Text(
                    text = "Verifikasi Master Password",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Untuk keamanan, masukkan master password untuk memulihkan data dari backup.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.restorePasswordInput,
                        onValueChange = viewModel::onRestorePasswordChange,
                        label = { Text("Master Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.restorePasswordError,
                        supportingText = { if (state.restorePasswordError) Text("Master Password Salah!") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmRestore
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRestoreCancel) {
                    Text("Batal")
                }
            }
        )
    }
}
