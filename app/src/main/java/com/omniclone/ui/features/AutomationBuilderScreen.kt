package com.omniclone.ui.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.omniclone.model.AutomationStep
import java.util.UUID

/**
 * Visual action sequencer for automation rules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationBuilderScreen(navController: NavController) {
    var sequenceName by remember { mutableStateOf("") }
    val steps = remember { mutableStateListOf<AutomationStep>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automation Builder") },
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
            OutlinedTextField(
                value = sequenceName,
                onValueChange = { sequenceName = it },
                label = { Text("Sequence name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    steps.add(
                        AutomationStep(
                            id = UUID.randomUUID().toString(),
                            action = "tap",
                            target = "",
                            delayMs = 500
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Add Tap Step")
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(steps, key = { it.id }) { step ->
                    AutomationStepRow(
                        step = step,
                        onUpdate = { updated ->
                            val index = steps.indexOf(step)
                            if (index >= 0) steps[index] = updated
                        },
                        onDelete = { steps.remove(step) }
                    )
                }
            }

            Button(
                onClick = { /* Save sequence */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Sequence")
            }
        }
    }
}

@Composable
private fun AutomationStepRow(
    step: AutomationStep,
    onUpdate: (AutomationStep) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = "${step.action.uppercase()} step", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = step.target ?: "",
            onValueChange = { onUpdate(step.copy(target = it)) },
            label = { Text("Target (text/resource-id)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = step.value ?: "",
            onValueChange = { onUpdate(step.copy(value = it)) },
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = step.delayMs.toString(),
                onValueChange = { onUpdate(step.copy(delayMs = it.toLongOrNull() ?: 0)) },
                label = { Text("Delay ms") },
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}
