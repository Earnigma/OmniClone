package com.omniclone.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Help screen with known incompatible apps list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    val incompatibleApps = listOf(
        "Snapchat", "WhatsApp", "WeChat", "Pokémon GO", "GCash", "Dana", "OVO", "Grab",
        "Tokopedia", "Lazada", "AliExpress", "Evernote", "Kodi", "Trello", "Viber",
        "Skype for Business", "OneDrive", "Yandex Go", "YouTube", "Google Play Services",
        "Google Play Games", "Gmail", "Google Maps"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Known Incompatible Apps",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "These apps cannot be cloned due to certificate pinning, deep platform integration, or safety checks.",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            incompatibleApps.forEach { app ->
                Text(
                    text = "• $app",
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Text(
                text = "Note: Google login, in-app purchases, and Google Play Games will NOT work in any clone.",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
