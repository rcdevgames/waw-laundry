package net.rcdevgames.wawlaundry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.rcdevgames.wawlaundry.data.local.SecurityPrefs
import net.rcdevgames.wawlaundry.ui.navigation.Screen
import net.rcdevgames.wawlaundry.ui.navigation.WawLaundryNavGraph
import net.rcdevgames.wawlaundry.ui.theme.WawLaundryTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var securityPrefs: SecurityPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val startDest = if (securityPrefs.hasSetupSecurity()) {
            Screen.CashierHome.route
        } else {
            Screen.SetupSecurity.route
        }

        setContent {
            WawLaundryTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WawLaundryNavGraph(
                        navController = navController,
                        startDestination = startDest
                    )
                }
            }
        }
    }
}