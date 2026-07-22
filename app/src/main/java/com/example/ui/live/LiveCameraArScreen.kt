package com.example.ui.live

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
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
import com.example.ar.HawkeyeDrsCard
import com.example.camera.CameraPreview
import com.example.engine.BallTrackerPhysicsEngine
import com.example.engine.DeliveryPhysicsInput
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
    var physicsInput by remember { mutableStateOf(DeliveryPhysicsInput()) }
    var showDrsPanel by remember { mutableStateOf(true) }

    val drsResult by remember(physicsInput) {
        derivedStateOf {
            BallTrackerPhysicsEngine.calculateDeliveryTrajectory(physicsInput)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Live Feed Preview
        CameraPreview(modifier = Modifier.fillMaxSize())

        // 2. AR Pitch 3D Math Trajectory Overlay
        ArPitchOverlay(
            modifier = Modifier.fillMaxSize(),
            drsResult = drsResult,
            physicsInput = physicsInput,
            onPitchTap = { xFrac, yOffset ->
                physicsInput = physicsInput.copy(
                    pitchFraction = xFrac,
                    padLateralOffsetMeters = yOffset
                )
            }
        )

        // 3. Floating HUD & Controls Layer
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
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDrsPanel = !showDrsPanel }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Toggle DRS Physics", tint = NeonGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGlass)
            )

            // Middle / Bottom Layer with DRS Card & Quick Scoring
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showDrsPanel) {
                    HawkeyeDrsCard(
                        drsResult = drsResult,
                        physicsInput = physicsInput,
                        onUpdatePhysicsInput = { newInput -> physicsInput = newInput }
                    )
                }

                // Quick Camera Scoring Bar
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0, 1, 2, 4, 6).forEach { run ->
                                Button(
                                    onClick = { onRecordBall(run, ExtraType.NONE, 0, WicketType.NONE, null, null) },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (run == 4 || run == 6) NeonGreen else Color.White.copy(alpha = 0.2f),
                                        contentColor = if (run == 4 || run == 6) Color.Black else Color.White
                                    ),
                                    modifier = Modifier.size(42.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("$run", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Button(
                                onClick = { onRecordBall(0, ExtraType.NONE, 0, WicketType.LBW, matchState.strikerName, null) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744), contentColor = Color.White),
                                modifier = Modifier.size(42.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("W", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
