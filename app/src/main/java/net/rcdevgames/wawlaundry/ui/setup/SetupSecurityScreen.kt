package net.rcdevgames.wawlaundry.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupSecurityScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupSecurityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var masterPasswordVisible by remember { mutableStateOf(false) }
    var ownerPinVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSetupComplete) {
        if (state.isSetupComplete) {
            onSetupComplete()
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
                title = { Text("Pengaturan Keamanan Awal") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Amankan Akses Database",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Master Password digunakan untuk mengenkripsi database lokal agar aman dipakai walau offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.masterPassword,
                onValueChange = viewModel::onMasterPasswordChange,
                label = { Text("Master Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (masterPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (masterPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { masterPasswordVisible = !masterPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.masterPasswordConfirm,
                onValueChange = viewModel::onMasterPasswordConfirmChange,
                label = { Text("Konfirmasi Master Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (masterPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = !state.isPasswordMatch,
                supportingText = { if (!state.isPasswordMatch) Text("Password tidak sama") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Owner PIN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "PIN digunakan untuk login sebagai Owner/Pemilik untuk melihat laporan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state.ownerPin,
                onValueChange = viewModel::onOwnerPinChange,
                label = { Text("PIN Owner (6 Digit)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (ownerPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                trailingIcon = {
                    val image = if (ownerPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { ownerPinVisible = !ownerPinVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle PIN visibility")
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.ownerPinConfirm,
                onValueChange = viewModel::onOwnerPinConfirmChange,
                label = { Text("Konfirmasi PIN") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (ownerPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = !state.isPinMatch,
                supportingText = { if (!state.isPinMatch) Text("PIN tidak sama") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::saveSecuritySettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Simpan Keamanan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
