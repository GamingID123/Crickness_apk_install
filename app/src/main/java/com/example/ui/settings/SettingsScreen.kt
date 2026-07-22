package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.AppSettings
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateDarkTheme: (Boolean) -> Unit,
    onUpdateDynamicColors: (Boolean) -> Unit,
    onUpdateDefaultOvers: (Int) -> Unit,
    onUpdateDefaultWickets: (Int) -> Unit,
    onUpdateAutoSave: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopAppBar(
                title = { Text("SETTINGS", color = NeonGreen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "About", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display & Theme
                GlassCard {
                    Text("THEME & COLORS", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme Canvas", color = Color.White)
                        Switch(
                            checked = settings.darkTheme,
                            onCheckedChange = onUpdateDarkTheme,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Material You Dynamic Colors", color = Color.White)
                        Switch(
                            checked = settings.dynamicColors,
                            onCheckedChange = onUpdateDynamicColors,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                        )
                    }
                }

                // Match Defaults
                GlassCard {
                    Text("DEFAULT MATCH FORMAT", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Default Overs: ${settings.defaultOvers}", color = Color.White)
                    Slider(
                        value = settings.defaultOvers.toFloat().coerceIn(1f, 90f),
                        onValueChange = { onUpdateDefaultOvers(it.toInt()) },
                        valueRange = 1f..90f,
                        steps = 88,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Default Wickets: ${settings.defaultWickets}", color = Color.White)
                    Slider(
                        value = settings.defaultWickets.toFloat().coerceIn(1f, 11f),
                        onValueChange = { onUpdateDefaultWickets(it.toInt()) },
                        valueRange = 1f..11f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Auto Save & Persistence
                GlassCard {
                    Text("DATA & PRIVACY", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Save Match Progress", color = Color.White)
                        Switch(
                            checked = settings.autoSave,
                            onCheckedChange = onUpdateAutoSave,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("ABOUT CRICKNESS AR", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Crickness – Gully AR Umpire Pro is a modern offline Android app for organizing, scoring, and visually assisting gully cricket matches with CameraX, AR pitch calibration, Hawkeye decision tracking, and Room database persistence.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK", color = NeonGreen) }
            },
            containerColor = DarkGlass
        )
    }
}
