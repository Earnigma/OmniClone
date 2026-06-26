package com.omniclone.ui.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

/**
 * Screen for viewing and editing spoofed identity values.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityManagerScreen(
    navController: NavController,
    viewModel: IdentityViewModel = viewModel()
) {
    val identity by viewModel.identity.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identity Manager") },
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
            Button(
                onClick = { viewModel.randomizeAll() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New Identity")
            }

            IdentityField("Android ID", identity.androidId) {
                viewModel.updateIdentity { copy(androidId = it) }
            }
            IdentityField("IMEI", identity.imei) {
                viewModel.updateIdentity { copy(imei = it) }
            }
            IdentityField("IMSI", identity.imsi) {
                viewModel.updateIdentity { copy(imsi = it) }
            }
            IdentityField("Wi-Fi MAC", identity.wifiMac) {
                viewModel.updateIdentity { copy(wifiMac = it) }
            }
            IdentityField("Bluetooth MAC", identity.bluetoothMac) {
                viewModel.updateIdentity { copy(bluetoothMac = it) }
            }
            IdentityField("Google Ad ID", identity.googleAdvertisingId) {
                viewModel.updateIdentity { copy(googleAdvertisingId = it) }
            }
            IdentityField("WebView UA", identity.webViewUserAgent) {
                viewModel.updateIdentity { copy(webViewUserAgent = it) }
            }
        }
    }
}

@Composable
private fun IdentityField(label: String, value: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
