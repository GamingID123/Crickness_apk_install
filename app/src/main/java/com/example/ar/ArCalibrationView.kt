package com.example.ar

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun ArCalibrationView(
    onCalibrationComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Camera Perm, 2: Pitch Grid, 3: Crease Alignment, 4: Complete
    var progress by remember { mutableFloatStateOf(0.25f) }
    var isCalibrating by remember { mutableStateOf(false) }

    LaunchedEffect(isCalibrating) {
        if (isCalibrating) {
            delay(800)
            step = 2
            progress = 0.5f
            delay(1000)
            step = 3
            progress = 0.75f
            delay(1200)
            step = 4
            progress = 1.0f
            isCalibrating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGlass),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AR PITCH CALIBRATION",
                    color = NeonGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (step) {
                        1 -> "Point camera towards gully pitch & hold steady"
                        2 -> "Detecting pitch surface dimensions..."
                        3 -> "Aligning bowling crease & popping crease..."
                        else -> "Pitch Calibration Complete!"
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonGreen,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }

        // Center Viewfinder Graphic
        Box(
            modifier = Modifier
                .size(240.dp)
                .border(2.dp, if (step == 4) NeonGreen else Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (step) {
                    1 -> Icons.Default.CropFree
                    2 -> Icons.Default.Straighten
                    3 -> Icons.Default.ScreenRotation
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = if (step == 4) NeonGreen else Color.White,
                modifier = Modifier.size(72.dp)
            )
        }

        // Bottom Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkGlass),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step < 4) {
                    Button(
                        onClick = { isCalibrating = true },
                        enabled = !isCalibrating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isCalibrating) "CALIBRATING..." else "START CALIBRATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onCalibrationComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "START MATCH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
