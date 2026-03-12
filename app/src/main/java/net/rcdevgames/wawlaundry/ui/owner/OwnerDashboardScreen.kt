package net.rcdevgames.wawlaundry.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToPromos: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    onNavigateToData: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Cashier")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Manajemen Master Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            DashboardMenuCard(
                title = "Layanan & Harga",
                subtitle = "Atur jenis cucian, harga per kilo atau pcs",
                icon = Icons.Default.LocalLaundryService,
                onClick = onNavigateToServices
            )

            DashboardMenuCard(
                title = "Promo & Diskon",
                subtitle = "Buat diskon persen atau nominal potongan",
                icon = Icons.Default.LocalOffer,
                onClick = onNavigateToPromos
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Keuangan & Laporan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            DashboardMenuCard(
                title = "Pengeluaran (Beban)",
                subtitle = "Catat biaya deterjen, listrik, dll",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                onClick = onNavigateToExpenses
            )

            DashboardMenuCard(
                title = "Laporan Pendapatan",
                subtitle = "Analisis profitabilitas dan grafik pemasukan",
                icon = Icons.Default.BarChart,
                onClick = onNavigateToReports
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Pengaturan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DashboardMenuCard(
                title = "Pengaturan Toko",
                subtitle = "Nama usaha, alamat, dan header/footer struk",
                icon = Icons.Default.Storefront,
                onClick = onNavigateToStore
            )

            DashboardMenuCard(
                title = "Pengaturan Printer",
                subtitle = "Pilih printer Bluetooth kasir",
                icon = Icons.Default.Print,
                onClick = onNavigateToPrinter
            )

            DashboardMenuCard(
                title = "Pengaturan Data & Reset",
                subtitle = "Backup, Restore, atau hapus database",
                icon = Icons.Default.Storage,
                onClick = onNavigateToData
            )
        }
    }
}

@Composable
fun DashboardMenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
