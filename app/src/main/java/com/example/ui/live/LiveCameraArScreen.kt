package com.example.ui.live

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.ArPitchOverlay
import com.example.camera.CameraPreview
import com.example.engine.MatchEngineState
import com.example.models.ExtraType
import com.example.models.WicketType
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCameraArScreen(
    matchState: MatchEngineState,
    onRecordBall: (runs: Int, extraType: ExtraType, extraRuns: Int, wicketType: WicketType, dismissedPlayer: String?, wagonDegree: Float?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var arDecision by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraPreview(modifier = Modifier.fillMaxSize())

        // AR Pitch & Trajectory Overlay
        ArPitchOverlay(
            modifier = Modifier.fillMaxSize(),
            decisionType = arDecision
        )

        // UI Controls & Top Bar HUD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AR UMPIRE LIVE HUD",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${matchState.currentRuns}/${matchState.currentWickets} in ${matchState.oversFormatted} ov • CRR: %.2f".format(matchState.currentRunRate),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            arDecision = if (arDecision == null) "OUT (LBW HITTING)" else if (arDecision!!.contains("OUT")) "NOT OUT (PITCHING OUTSIDE)" else null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HAWKEYE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGlass)
            )

            // Bottom Floating Quick Scoring Bar
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUICK CAMERA SCORING",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(0, 1, 2, 4, 6).forEach { run ->
                            Button(
                                onClick = { onRecordBall(run, ExtraType.NONE, 0, WicketType.NONE, null, null) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (run == 4 || run == 6) NeonGreen else Color.White.copy(alpha = 0.2f),
                                    contentColor = if (run == 4 || run == 6) Color.Black else Color.White
                                ),
                                modifier = Modifier.size(44.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("$run", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Button(
                            onClick = { onRecordBall(0, ExtraType.NONE, 0, WicketType.BOWLED, matchState.strikerName, null) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744), contentColor = Color.White),
                            modifier = Modifier.size(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("W", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
