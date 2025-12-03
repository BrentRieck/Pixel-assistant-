package com.example.pixelassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.material3.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixelAssistantScreen(viewModel = chatViewModel)
        }
    }
}

@Composable
fun PixelAssistantScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TopBar(
            selectedModel = state.selectedModel,
            onModelSelected = { viewModel.changeModel(it, clearHistory = false) },
            isMemoryEnabled = state.isMemoryEnabled,
            onMemoryChanged = viewModel::toggleMemory,
            onClearConversation = viewModel::clearConversation,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages) { message ->
                MessageBubble(message = message)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        InputRow(
            value = state.input,
            onValueChange = viewModel::updateInput,
            onSend = viewModel::sendMessage,
            isSending = state.isSending,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    selectedModel: ModelCard,
    onModelSelected: (ModelCard) -> Unit,
    isMemoryEnabled: Boolean,
    onMemoryChanged: (Boolean) -> Unit,
    onClearConversation: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    value = selectedModel.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors(disabledIndicatorColor = MaterialTheme.colorScheme.primary),
                    enabled = false,
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ModelCatalog.featuredModels.forEach { card ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = card.displayName) },
                            onClick = {
                                expanded = false
                                onModelSelected(card)
                            },
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Memory")
                Switch(checked = isMemoryEnabled, onCheckedChange = onMemoryChanged)
            }
            IconButton(onClick = onClearConversation) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Clear")
            }
        }
        Text(
            text = selectedModel.description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message is ChatMessage.User
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Text(
            text = if (isUser) "You" else "Pixel Assistant",
            style = MaterialTheme.typography.labelSmall,
        )
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = message.content,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun InputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = "Ask anything offline...") },
            enabled = !isSending,
            singleLine = false,
            minLines = 1,
            maxLines = 4,
        )
        Button(onClick = onSend, enabled = !isSending) {
            Icon(Icons.Filled.Send, contentDescription = null)
            Text(text = "Send", modifier = Modifier.padding(start = 4.dp))
        }
    }
}
