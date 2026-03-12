package net.rcdevgames.wawlaundry.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinUnlockScreen(
    onUnlockSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PinUnlockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Keypad number order layout generated once per screen open
    var shuffledKeys by remember { mutableStateOf((0..9).shuffled()) }

    LaunchedEffect(state.isUnlocked) {
        if (state.isUnlocked) {
            onUnlockSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akses Owner") },
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Masukkan PIN Owner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ketik PIN melalui keypad di bawah.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 6) {
                    val isActive = i < state.pinInput.length
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.isError) {
                Text(
                    text = "PIN Salah!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = " ", // placeholder to maintain vertical space
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Randomized Keypad
            val keys = shuffledKeys
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0..2) {
                            val digit = keys[row * 3 + col]
                            KeypadButton(text = digit.toString()) {
                                if (state.pinInput.length < 6) {
                                    // We clear the error immediately on new input by replacing or appending
                                    viewModel.onPinChange(if (state.isError) digit.toString() else state.pinInput + digit)
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KeypadButton(
                        text = "Batal", 
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        fontSizeSp = 16
                    ) {
                        onNavigateBack()
                    }
                    val lastDigit = keys[9]
                    KeypadButton(text = lastDigit.toString()) {
                        if (state.pinInput.length < 6) {
                            viewModel.onPinChange(if (state.isError) lastDigit.toString() else state.pinInput + lastDigit)
                        }
                    }
                    KeypadButton(
                        text = "Hapus", 
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSizeSp = 16
                    ) {
                        if (state.pinInput.isNotEmpty()) {
                            viewModel.onPinChange(if (state.isError) "" else state.pinInput.dropLast(1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSizeSp: Int = 24,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
