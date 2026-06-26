package com.omniclone.ui.clone

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.omniclone.data.CloneStats

/**
 * Screen showing per-clone details, stats, and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneDetailScreen(
    cloneId: String,
    navController: NavController,
    viewModel: CloneManagerViewModel = hiltViewModel()
) {
    val clones by viewModel.clones.collectAsState()
    val clone = remember(clones, cloneId) { clones.find { it.cloneId == cloneId } }
    var stats by remember { mutableStateOf<CloneStats?>(null) }

    LaunchedEffect(cloneId) {
        viewModel.getStats(cloneId) { stats = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(clone?.cloneName ?: "Clone Details") },
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
            clone?.let { config ->
                Text("Package", style = MaterialTheme.typography.labelMedium)
                Text(config.clonePackage, style = MaterialTheme.typography.bodyLarge)

                Text(
                    "Index",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(config.cloneIndex.toString(), style = MaterialTheme.typography.bodyLarge)

                Text(
                    "Features enabled",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    config.features.size.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )

                stats?.let {
                    Text(
                        "Storage",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        "${it.storageBytes / 1024 / 1024} MB",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Launches: ${it.launchCount}",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Button(
                    onClick = { /* Reclone */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text("Reclone")
                }
                Button(
                    onClick = { viewModel.deleteClone(cloneId); navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Delete")
                }
            } ?: Text("Clone not found")
        }
    }
}
