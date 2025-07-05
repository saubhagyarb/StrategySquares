package com.saubh.strategysquares.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.saubh.strategysquares.ui.theme.GameSymbols

@Composable
fun SymbolPickerDialog(
    currentSymbol: String,
    currentColor: Long,
    onDismiss: () -> Unit,
    onSymbolSelected: (String, Long) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(currentSymbol) }
    var selectedColor by remember {
        mutableStateOf(
            GameSymbols.defaultSymbols.find { it.first == currentSymbol }?.second ?: currentColor
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Your Symbol",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of symbols
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(GameSymbols.defaultSymbols) { symbolPair ->
                        SymbolItem(
                            symbol = symbolPair.first,
                            color = symbolPair.second,
                            isSelected = symbolPair.first == selectedSymbol,
                            onClick = {
                                selectedSymbol = symbolPair.first
                                selectedColor = symbolPair.second
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSymbolSelected(selectedSymbol, selectedColor)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun SymbolItem(
    symbol: String,
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        border = ButtonDefaults.outlinedButtonBorder,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(color)
            )
        }
    }
}
