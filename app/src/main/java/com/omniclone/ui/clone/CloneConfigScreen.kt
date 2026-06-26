package com.omniclone.ui.clone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.omniclone.model.FeatureKey
import com.omniclone.ui.config.CloneConfigViewModel
import com.omniclone.ui.config.FeatureGroups

/**
 * Screen for configuring clone features before building.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneConfigScreen(
    navController: NavController,
    viewModel: CloneConfigViewModel = hiltViewModel()
) {
    val features by viewModel.features.collectAsState()
    val cloneName by viewModel.cloneName.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Clone") },
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
        ) {
            OutlinedTextField(
                value = cloneName,
                onValueChange = viewModel::setCloneName,
                label = { Text("Clone name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )

            Text(
                text = "Source: ${selectedApp?.packageName ?: "Unknown"}",
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            FeatureGroups.groups.forEach { group ->
                var expanded by remember { mutableStateOf(false) }

                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(12.dp)
                )

                if (expanded) {
                    group.items.forEach { item ->
                        val isEnabled = features.containsKey(item.key)
                        val inputValue = features[item.key] ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.toggleFeature(item.key, checked)
                                }
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(text = item.label)
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (item.isInput && isEnabled) {
                            OutlinedTextField(
                                value = inputValue,
                                onValueChange = { viewModel.setFeatureValue(item.key, it) },
                                label = { Text(item.label) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.startClone()
                    navController.navigate("clone_progress")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text("Build Clone")
            }
        }
    }
}


