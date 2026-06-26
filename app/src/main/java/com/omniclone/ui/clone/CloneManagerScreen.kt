package com.omniclone.ui.clone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.omniclone.model.CloneConfig

/**
 * Main dashboard showing all clones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneManagerScreen(
    navController: NavController,
    viewModel: CloneManagerViewModel = hiltViewModel()
) {
    val clones by viewModel.clones.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OmniClone") },
                actions = {
                    IconButton(onClick = { navController.navigate("identity_manager") }) {
                        Text("ID")
                    }
                    IconButton(onClick = { navController.navigate("gps_spoof") }) {
                        Text("GPS")
                    }
                    IconButton(onClick = { navController.navigate("automation_builder") }) {
                        Text("Auto")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Text("⚙")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("app_selector") }) {
                Icon(Icons.Default.Add, contentDescription = "Create clone")
            }
        }
    ) { padding ->
        if (clones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No clones yet. Tap + to create one.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clones, key = { it.cloneId }) { clone ->
                    CloneCard(
                        clone = clone,
                        onClick = { navController.navigate("clone_detail/${clone.cloneId}") },
                        onDelete = { viewModel.deleteClone(clone.cloneId) },
                        onReclone = { /* TODO in detail screen */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun CloneCard(
    clone: CloneConfig,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onReclone: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = clone.cloneName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reclone") },
                        onClick = { expanded = false; onReclone() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { expanded = false; onDelete() }
                    )
                }
            }
            Text(
                text = clone.clonePackage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "v${clone.cloneIndex}",
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
