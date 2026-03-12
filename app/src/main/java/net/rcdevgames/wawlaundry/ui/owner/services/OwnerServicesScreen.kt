package net.rcdevgames.wawlaundry.ui.owner.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.rcdevgames.wawlaundry.data.local.entity.ServiceEntity
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerServicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerServicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = CurrencyFormatter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layanan & Harga") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Layanan")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Daftar layanan kosong.\nSilakan tambah layanan laundry baru.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.services) { service ->
                    ServiceOwnerCard(
                        service = service,
                        currencyFormat = currencyFormat.getNumberFormat(),
                        onEdit = { viewModel.toggleDialog(true, service) },
                        onDelete = { viewModel.deleteService(service.id) }
                    )
                }
            }
        }
    }

    if (state.showDialog) {
        ServiceFormDialog(viewModel = viewModel)
    }
}

@Composable
fun ServiceOwnerCard(
    service: ServiceEntity,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(service.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ServiceFormDialog(viewModel: OwnerServicesViewModel) {
    val state by viewModel.state.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.toggleDialog(false) },
        title = { Text(if (state.editServiceId == null) "Tambah Layanan" else "Edit Layanan") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nama Layanan (Cth: Cuci Kering)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.priceInput,
                        onValueChange = viewModel::onPriceChange,
                        label = { Text("Harga (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

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
            TextButton(onClick = viewModel::saveService) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.toggleDialog(false) }) {
                Text("Batal")
            }
        }
    )
}
