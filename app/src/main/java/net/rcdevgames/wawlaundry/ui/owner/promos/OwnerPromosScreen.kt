package net.rcdevgames.wawlaundry.ui.owner.promos

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
import net.rcdevgames.wawlaundry.data.local.entity.PromoEntity
import net.rcdevgames.wawlaundry.data.local.entity.PromoType
import net.rcdevgames.wawlaundry.util.CurrencyFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPromosScreen(
    onNavigateBack: () -> Unit,
    viewModel: OwnerPromosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = CurrencyFormatter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seting Promo & Diskon") },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Promo")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.promos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Daftar promo kosong.\nSilakan buat promo diskon baru.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.promos) { promo ->
                    PromoOwnerCard(
                        promo = promo,
                        currencyFormat = currencyFormat.getNumberFormat(),
                        onEdit = { viewModel.toggleDialog(true, promo) },
                        onDelete = { viewModel.deletePromo(promo.id) }
                    )
                }
            }
        }
    }

    if (state.showDialog) {
        PromoFormDialog(viewModel = viewModel)
    }
}

@Composable
fun PromoOwnerCard(
    promo: PromoEntity,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayValue = if (promo.promoType == PromoType.PERCENTAGE) {
        "${promo.value.toInt()}%"
    } else {
        currencyFormat.format(promo.value)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (promo.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = promo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!promo.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Nonaktif", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            border = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Diskon: $displayValue",
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
fun PromoFormDialog(viewModel: OwnerPromosViewModel) {
    val state by viewModel.state.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.toggleDialog(false) },
        title = { Text(if (state.editPromoId == null) "Tambah Promo" else "Edit Promo") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.titleInput,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Nama Promo (Cth: Diskon Lebaran)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "Tipe Diskon", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.typeInput == PromoType.PERCENTAGE,
                        onClick = { viewModel.onTypeChange(PromoType.PERCENTAGE) },
                        label = { Text("Persentase (%)") }
                    )
                    FilterChip(
                        selected = state.typeInput == PromoType.NOMINAL,
                        onClick = { viewModel.onTypeChange(PromoType.NOMINAL) },
                        label = { Text("Nominal (Rp)") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.valueInput,
                    onValueChange = viewModel::onValueChange,
                    label = { Text(if (state.typeInput == PromoType.PERCENTAGE) "Potongan Persen (%)" else "Potongan Rupiah (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.isActiveInput,
                        onCheckedChange = viewModel::onActiveChange
                    )
                    Text(text = "Promo Aktif", style = MaterialTheme.typography.bodyMedium)
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
            TextButton(onClick = viewModel::savePromo) {
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
