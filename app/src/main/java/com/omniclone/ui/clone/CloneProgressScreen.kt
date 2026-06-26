package com.omniclone.ui.clone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.WorkManager
import kotlinx.coroutines.delay

/**
 * Screen showing live clone pipeline progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneProgressScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") workManager: WorkManager
) {
    var step by remember { mutableStateOf("EXTRACT") }
    var message by remember { mutableStateOf("Preparing...") }
    var percent by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // In a real implementation this would observe WorkManager progress by work request ID.
        // For now we simulate progress for UI demonstration.
        val steps = listOf(
            "EXTRACT" to "Extracting source APK...",
            "DECODE" to "Decoding with ApkTool...",
            "PATCH MANIFEST" to "Patching AndroidManifest.xml...",
            "PATCH SMALI" to "Injecting Smali hooks...",
            "REBUILD" to "Rebuilding patched APK...",
            "ZIPALIGN" to "Aligning APK...",
            "SIGN" to "Signing APK...",
            "INSTALL" to "Requesting installation..."
        )
        steps.forEachIndexed { index, (s, m) ->
            step = s
            message = m
            percent = ((index + 1) * 100) / steps.size
            kotlinx.coroutines.delay(600)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Building Clone") },
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
                .padding(16.dp)
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "$percent%",
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = { navController.popBackStack("clone_manager", inclusive = false) },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Done")
            }
        }
    }
}
