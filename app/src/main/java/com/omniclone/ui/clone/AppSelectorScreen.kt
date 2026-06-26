package com.omniclone.ui.clone

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.omniclone.ui.config.CloneConfigViewModel

/**
 * Screen for selecting the source app to clone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorScreen(
    navController: NavController,
    viewModel: CloneConfigViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState()
    var query by remember { mutableStateOf("") }
    var showSystem by remember { mutableStateOf(false) }

    LaunchedEffect(showSystem) {
        viewModel.loadInstalledApps(showSystem)
    }

    val context = LocalContext.current
    val pm = context.packageManager

    val filtered = apps.filter {
        val label = it.loadLabel(pm).toString()
        label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select App") },
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
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Checkbox(checked = showSystem, onCheckedChange = { showSystem = it })
                Text("Show system apps")
            }
            LazyColumn {
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(app = app, pm = pm, onClick = {
                        viewModel.selectApp(app)
                        navController.navigate("clone_config")
                    })
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: ApplicationInfo, pm: PackageManager, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon: Drawable = remember(app.packageName) { app.loadIcon(pm) }
        Image(
            bitmap = icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = app.loadLabel(pm).toString())
            Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
        }
    }
}
