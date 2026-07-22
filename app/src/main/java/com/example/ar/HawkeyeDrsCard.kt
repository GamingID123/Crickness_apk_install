package com.example.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen

@Composable
fun HawkeyeDrsCard(
    drsResult: DrsDecisionResult,
    physicsInput: DeliveryPhysicsInput,
    onUpdatePhysicsInput: (DeliveryPhysicsInput) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedControls by remember { mutableStateOf(false) }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Title & Verdict Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonGreen)
                    Text("HAWK-EYE DRS PHYSICS ENGINE", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { expandedControls = !expandedControls }) {
                    Text(if (expandedControls) "Hide Physics" else "Adjust Delivery", color = Color.White, fontSize = 12.sp)
                }
            }

            // Summary 3-Zone Breakdown (Pitching - Impact - Wickets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Pitching Zone Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PITCHING", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            text = drsResult.pitchingZone.name,
                            color = when (drsResult.pitchingZone) {
                                PitchingZone.IN_LINE -> NeonGreen
                                PitchingZone.OUTSIDE_OFF -> Color(0xFF00E5FF)
                                PitchingZone.OUTSIDE_LEG -> Color(0xFFFF1744)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // 2. Impact Zone Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("IMPACT", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            text = drsResult.impactZone.name,
                            color = when (drsResult.impactZone) {
                                ImpactZone.IN_LINE -> NeonGreen
                                else -> Color(0xFFFF9100)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // 3. Stumps Hit Status Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WICKETS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            text = drsResult.stumpHitStatus.name,
                            color = if (drsResult.stumpHitStatus == StumpHitStatus.HITTING) Color(0xFFFF1744) else NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Expanded Physics Delivery Controls Sliders
            if (expandedControls) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Text("PHYSICS INPUT PARAMETERS", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Speed Slider
                Column {
                    Text("Release Speed: %.1f km/h".format(physicsInput.releaseSpeedKph), color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = physicsInput.releaseSpeedKph,
                        onValueChange = { onUpdatePhysicsInput(physicsInput.copy(releaseSpeedKph = it)) },
                        valueRange = 90f..155f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Pitch Length Slider
                Column {
                    val lengthLabel = when {
                        physicsInput.pitchFraction < 0.45f -> "Full Pitch / Yorker"
                        physicsInput.pitchFraction < 0.75f -> "Good Length"
                        else -> "Short Pitch / Bouncer"
                    }
                    Text("Pitch Length: $lengthLabel (%.2f)".format(physicsInput.pitchFraction), color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = physicsInput.pitchFraction,
                        onValueChange = { onUpdatePhysicsInput(physicsInput.copy(pitchFraction = it)) },
                        valueRange = 0.25f..0.85f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Swing / Spin Degrees
                Column {
                    Text("Swing / Movement Angle: %.1f°".format(physicsInput.swingDegrees), color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = physicsInput.swingDegrees,
                        onValueChange = { onUpdatePhysicsInput(physicsInput.copy(swingDegrees = it)) },
                        valueRange = -5f..5f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Pad Impact Distance from Stumps
                Column {
                    Text("Impact Distance from Stumps: %.2f m".format(physicsInput.impactDistanceMeters), color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = physicsInput.impactDistanceMeters,
                        onValueChange = { onUpdatePhysicsInput(physicsInput.copy(impactDistanceMeters = it)) },
                        valueRange = 0.8f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Lateral Pad Offset
                Column {
                    Text("Lateral Pad Offset: %.2f m".format(physicsInput.padLateralOffsetMeters), color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = physicsInput.padLateralOffsetMeters,
                        onValueChange = { onUpdatePhysicsInput(physicsInput.copy(padLateralOffsetMeters = it)) },
                        valueRange = -0.20f..0.20f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }

                // Shot Offered & Batsman Hand Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Shot Offered:", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = physicsInput.shotOffered,
                            onCheckedChange = { onUpdatePhysicsInput(physicsInput.copy(shotOffered = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Right Handed:", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = physicsInput.isRightHandedBatsman,
                            onCheckedChange = { onUpdatePhysicsInput(physicsInput.copy(isRightHandedBatsman = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                        )
                    }
                }
            }

            // Scientific Log Text Details
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = drsResult.scientificDetails,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
