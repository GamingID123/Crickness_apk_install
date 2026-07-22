package com.example.ui.live

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MatchEngineState
import com.example.models.ExtraType
import com.example.models.InningsStatus
import com.example.models.WicketType
import com.example.ui.components.*
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchState: MatchEngineState,
    onRecordBall: (runs: Int, extraType: ExtraType, extraRuns: Int, wicketType: WicketType, dismissedPlayer: String?, wagonDegree: Float?) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSwapBatsmen: () -> Unit,
    onSetBowler: (String) -> Unit,
    onStartSecondInnings: (striker: String, nonStriker: String, bowler: String) -> Unit,
    onNavigateCameraAr: () -> Unit,
    onNavigateScoreboard: () -> Unit,
    onNavigateSaveMatch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showWicketDialog by remember { mutableStateOf(false) }
    var showBowlerDialog by remember { mutableStateOf(false) }
    var showWagonWheel by remember { mutableStateOf(false) }
    var selectedWagonDegree by remember { mutableStateOf<Float?>(null) }

    var selectedWicketType by remember { mutableStateOf(WicketType.BOWLED) }
    var newBowlerName by remember { mutableStateOf("") }

    // Auto navigate to save when match completed
    LaunchedEffect(matchState.inningsStatus) {
        if (matchState.inningsStatus == InningsStatus.COMPLETED) {
            onNavigateSaveMatch()
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${matchState.battingTeam} vs ${matchState.bowlingTeam}",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Innings ${matchState.currentInnings} • ${matchState.matchType.name}",
                            color = Color.White.copy(alpha = 0.7f),
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
                    IconButton(onClick = onNavigateCameraAr) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera AR", tint = NeonGreen)
                    }
                    IconButton(onClick = onNavigateScoreboard) {
                        Icon(Icons.Default.Scoreboard, contentDescription = "Scoreboard", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Score Dashboard Glass Card
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = matchState.battingTeam.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedScore(
                                score = matchState.currentRuns,
                                wickets = matchState.currentWickets
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${matchState.oversFormatted} / ${matchState.maxOvers} Overs",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CRR: %.2f".format(matchState.currentRunRate),
                                color = NeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            matchState.requiredRunRate?.let { rrr ->
                                Text(
                                    text = "RRR: %.2f • Target: %d".format(rrr, matchState.target ?: 0),
                                    color = Color(0xFF00E5FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Partnership & Projection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Partnership: ${matchState.partnershipRuns} (${matchState.partnershipBalls}b)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Proj. Score: ${matchState.projectedScore}",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Current Batsmen & Bowler Stats Card
                GlassCard {
                    // Striker & Non-Striker
                    Text("BATTING", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val strikerStats = matchState.playerStatsMap[matchState.strikerName]
                    val nonStrikerStats = matchState.playerStatsMap[matchState.nonStrikerName]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "* ${matchState.strikerName}",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${strikerStats?.runs ?: 0} (${strikerStats?.ballsFaced ?: 0}b) • 4s: ${strikerStats?.fours ?: 0} 6s: ${strikerStats?.sixes ?: 0}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "  ${matchState.nonStrikerName}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${nonStrikerStats?.runs ?: 0} (${nonStrikerStats?.ballsFaced ?: 0}b) • 4s: ${nonStrikerStats?.fours ?: 0} 6s: ${nonStrikerStats?.sixes ?: 0}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Bowler Stats
                    val bowlerStats = matchState.playerStatsMap[matchState.bowlerName]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("BOWLING", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = matchState.bowlerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "${bowlerStats?.wicketsTaken ?: 0}-${bowlerStats?.runsConceded ?: 0} (${bowlerStats?.oversBowled ?: 0.0}ov)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Ball History Chips Row
                GlassCard {
                    Text("THIS OVER", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (matchState.currentOverBalls.isEmpty()) {
                            item {
                                Text(
                                    text = "Ready for over start...",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            items(matchState.currentOverBalls) { ball ->
                                val text = when {
                                    ball.wicketType != WicketType.NONE -> "W"
                                    ball.extraType == ExtraType.WIDE -> "${ball.runs + 1}wd"
                                    ball.extraType == ExtraType.NO_BALL -> "${ball.runs + 1}nb"
                                    else -> "${ball.runs}"
                                }
                                ScoreChip(
                                    text = text,
                                    isWicket = ball.wicketType != WicketType.NONE,
                                    isBoundary = ball.runs == 4,
                                    isSix = ball.runs == 6,
                                    isExtra = ball.extraType != ExtraType.NONE
                                )
                            }
                        }
                    }
                }

                // Wagon Wheel Toggle
                if (showWagonWheel) {
                    WagonWheel(
                        selectedDegree = selectedWagonDegree,
                        shotDegreesList = matchState.ballHistory.mapNotNull { it.wagonWheelDegree },
                        onDegreeSelected = { deg -> selectedWagonDegree = deg }
                    )
                }

                // Scoring Pad Controls
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MANUAL SCORING", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onUndo) {
                                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                            }
                            IconButton(onClick = onRedo) {
                                Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.White)
                            }
                            IconButton(onClick = onSwapBatsmen) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Batsmen", tint = NeonGreen)
                            }
                            IconButton(onClick = { showWagonWheel = !showWagonWheel }) {
                                Icon(Icons.Default.PieChart, contentDescription = "Wagon Wheel", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Runs Buttons Grid (0 to 6)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0, 1, 2, 3, 4, 5, 6).forEach { run ->
                            Button(
                                onClick = {
                                    onRecordBall(run, ExtraType.NONE, 0, WicketType.NONE, null, selectedWagonDegree)
                                    selectedWagonDegree = null
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (run == 4 || run == 6) NeonGreen else Color.White.copy(alpha = 0.15f),
                                    contentColor = if (run == 4 || run == 6) Color.Black else Color.White
                                ),
                                modifier = Modifier.size(42.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = "$run", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Extras Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRecordBall(0, ExtraType.WIDE, 1, WicketType.NONE, null, selectedWagonDegree) },
                            modifier = Modifier.weight(1f)
                        ) { Text("WIDE", fontSize = 12.sp) }

                        OutlinedButton(
                            onClick = { onRecordBall(0, ExtraType.NO_BALL, 1, WicketType.NONE, null, selectedWagonDegree) },
                            modifier = Modifier.weight(1f)
                        ) { Text("NO BALL", fontSize = 12.sp) }

                        OutlinedButton(
                            onClick = { onRecordBall(1, ExtraType.BYE, 0, WicketType.NONE, null, selectedWagonDegree) },
                            modifier = Modifier.weight(1f)
                        ) { Text("BYE", fontSize = 12.sp) }

                        OutlinedButton(
                            onClick = { onRecordBall(1, ExtraType.LEG_BYE, 0, WicketType.NONE, null, selectedWagonDegree) },
                            modifier = Modifier.weight(1f)
                        ) { Text("LEG BYE", fontSize = 12.sp) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wicket Button
                    Button(
                        onClick = { showWicketDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("OUT / WICKET", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Wicket Type Selection Dialog
    if (showWicketDialog) {
        AlertDialog(
            onDismissRequest = { showWicketDialog = false },
            title = { Text("SELECT DISMISSAL TYPE", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        WicketType.BOWLED,
                        WicketType.CAUGHT,
                        WicketType.RUN_OUT,
                        WicketType.LBW,
                        WicketType.STUMPED,
                        WicketType.HIT_WICKET
                    ).forEach { wType ->
                        Button(
                            onClick = {
                                showWicketDialog = false
                                onRecordBall(0, ExtraType.NONE, 0, wType, matchState.strikerName, selectedWagonDegree)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(wType.name, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWicketDialog = false }) { Text("CANCEL", color = Color.White) }
            },
            containerColor = DarkGlass
        )
    }

    // Innings Break Prompt Modal
    if (matchState.inningsStatus == InningsStatus.INNINGS_BREAK) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("INNINGS BREAK", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("First Innings Completed!", color = Color.White)
                    Text("Target Score: ${matchState.target}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val teamBPlayers = matchState.playerStatsMap.keys.filter { it !in matchState.teamAPlayers }.ifEmpty { listOf("Player B1", "Player B2", "Player B3") }
                        val teamAPlayers = matchState.teamAPlayers
                        onStartSecondInnings(
                            teamBPlayers.getOrElse(0) { "Striker B" },
                            teamBPlayers.getOrElse(1) { "NonStriker B" },
                            teamAPlayers.getOrElse(0) { "Bowler A" }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                ) {
                    Text("START 2ND INNINGS")
                }
            },
            containerColor = DarkGlass
        )
    }
}
