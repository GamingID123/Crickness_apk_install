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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SportsCricket
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

enum class PlayerEditRole { STRIKER, NON_STRIKER, BOWLER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchState: MatchEngineState,
    onRecordBall: (runs: Int, extraType: ExtraType, extraRuns: Int, wicketType: WicketType, dismissedPlayer: String?, wagonDegree: Float?) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSwapBatsmen: () -> Unit,
    onSetBowler: (String) -> Unit,
    onSetStriker: (String) -> Unit,
    onSetNonStriker: (String) -> Unit,
    onStartSecondInnings: (striker: String, nonStriker: String, bowler: String) -> Unit,
    onNavigateCameraAr: () -> Unit,
    onNavigateScoreboard: () -> Unit,
    onNavigateSaveMatch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showWicketDialog by remember { mutableStateOf(false) }
    var showEditPlayerDialog by remember { mutableStateOf<PlayerEditRole?>(null) }
    var showWagonWheel by remember { mutableStateOf(false) }
    var selectedWagonDegree by remember { mutableStateOf<Float?>(null) }

    // Wicket Dialog state
    var selectedWicketType by remember { mutableStateOf(WicketType.BOWLED) }
    var dismissedWho by remember { mutableStateOf("STRIKER") } // "STRIKER" or "NON_STRIKER"
    var newBatsmanNameInput by remember { mutableStateOf("") }

    // Edit Player state
    var editPlayerText by remember { mutableStateOf("") }

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

                // Who is Batting & Bowling Card (With Edit Player Name Options)
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WHO IS BATTING", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { onSwapBatsmen() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Strike", tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Swap Strike", color = NeonGreen, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val strikerStats = matchState.playerStatsMap[matchState.strikerName]
                    val nonStrikerStats = matchState.playerStatsMap[matchState.nonStrikerName]

                    // Striker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.12f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("🏏 * ", color = NeonGreen, fontWeight = FontWeight.Bold)
                            Text(
                                text = matchState.strikerName.ifBlank { "Striker" },
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            IconButton(
                                onClick = {
                                    editPlayerText = matchState.strikerName
                                    showEditPlayerDialog = PlayerEditRole.STRIKER
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Striker Name", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "${strikerStats?.runs ?: 0} (${strikerStats?.ballsFaced ?: 0}b) • 4s: ${strikerStats?.fours ?: 0} 6s: ${strikerStats?.sixes ?: 0}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Non-Striker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("🏃  ", color = Color.White)
                            Text(
                                text = matchState.nonStrikerName.ifBlank { "Non-Striker" },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            IconButton(
                                onClick = {
                                    editPlayerText = matchState.nonStrikerName
                                    showEditPlayerDialog = PlayerEditRole.NON_STRIKER
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Non-Striker Name", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "${nonStrikerStats?.runs ?: 0} (${nonStrikerStats?.ballsFaced ?: 0}b) • 4s: ${nonStrikerStats?.fours ?: 0} 6s: ${nonStrikerStats?.sixes ?: 0}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Bowler Row
                    val bowlerStats = matchState.playerStatsMap[matchState.bowlerName]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("BOWLING", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = matchState.bowlerName.ifBlank { "Bowler" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                IconButton(
                                    onClick = {
                                        editPlayerText = matchState.bowlerName
                                        showEditPlayerDialog = PlayerEditRole.BOWLER
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Bowler Name", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                }
                            }
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

                    // Wicket / OUT Button
                    Button(
                        onClick = {
                            newBatsmanNameInput = ""
                            showWicketDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("OUT / WICKET 🚨", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Wicket / Dismissal Dialog with Incoming Batsman
    if (showWicketDialog) {
        AlertDialog(
            onDismissRequest = { showWicketDialog = false },
            title = { Text("RECORD OUT / WICKET", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. WHO IS OUT?", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = dismissedWho == "STRIKER",
                            onClick = { dismissedWho = "STRIKER" },
                            label = { Text("Striker: ${matchState.strikerName}") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF1744),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = dismissedWho == "NON_STRIKER",
                            onClick = { dismissedWho = "NON_STRIKER" },
                            label = { Text("Non-Striker: ${matchState.nonStrikerName}") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF1744),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. DISMISSAL METHOD", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    listOf(
                        listOf(WicketType.BOWLED, WicketType.CAUGHT),
                        listOf(WicketType.RUN_OUT, WicketType.LBW),
                        listOf(WicketType.STUMPED, WicketType.HIT_WICKET)
                    ).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { wType ->
                                FilterChip(
                                    selected = selectedWicketType == wType,
                                    onClick = { selectedWicketType = wType },
                                    label = { Text(wType.name) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonGreen,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. NEW INCOMING BATSMAN NAME", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = newBatsmanNameInput,
                        onValueChange = { newBatsmanNameInput = it },
                        placeholder = { Text("Enter new batsman name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dismissedPlayerName = if (dismissedWho == "STRIKER") matchState.strikerName else matchState.nonStrikerName
                        val incomingBatsman = newBatsmanNameInput.ifBlank { "New Batsman ${matchState.currentWickets + 2}" }

                        // Record ball
                        onRecordBall(0, ExtraType.NONE, 0, selectedWicketType, dismissedPlayerName, selectedWagonDegree)

                        // Set incoming batsman
                        if (dismissedWho == "STRIKER") {
                            onSetStriker(incomingBatsman)
                        } else {
                            onSetNonStriker(incomingBatsman)
                        }

                        showWicketDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744), contentColor = Color.White)
                ) {
                    Text("CONFIRM OUT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWicketDialog = false }) { Text("CANCEL", color = Color.White) }
            },
            containerColor = DarkGlass
        )
    }

    // Edit Active Player Dialog (Striker, Non-Striker, Bowler)
    showEditPlayerDialog?.let { role ->
        AlertDialog(
            onDismissRequest = { showEditPlayerDialog = null },
            title = {
                Text(
                    text = when (role) {
                        PlayerEditRole.STRIKER -> "EDIT STRIKER NAME"
                        PlayerEditRole.NON_STRIKER -> "EDIT NON-STRIKER NAME"
                        PlayerEditRole.BOWLER -> "EDIT BOWLER NAME"
                    },
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter name:", color = Color.White)
                    OutlinedTextField(
                        value = editPlayerText,
                        onValueChange = { editPlayerText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = editPlayerText.ifBlank { "Player" }
                        when (role) {
                            PlayerEditRole.STRIKER -> onSetStriker(finalName)
                            PlayerEditRole.NON_STRIKER -> onSetNonStriker(finalName)
                            PlayerEditRole.BOWLER -> onSetBowler(finalName)
                        }
                        showEditPlayerDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                ) {
                    Text("SAVE NAME")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPlayerDialog = null }) { Text("CANCEL", color = Color.White) }
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
                        onStartSecondInnings("Opening Striker", "Opening Non-Striker", "Opening Bowler")
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
