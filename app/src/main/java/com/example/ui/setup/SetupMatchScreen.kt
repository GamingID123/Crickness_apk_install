package com.example.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.BallType
import com.example.models.MatchType
import com.example.models.TossChoice
import com.example.models.TossOption
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.theme.DarkGlass
import com.example.ui.theme.NeonGreen
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupMatchScreen(
    onNavigateCalibration: () -> Unit,
    onNavigateBack: () -> Unit,
    onSaveSetup: (
        teamA: String,
        teamB: String,
        teamAPlayers: List<String>,
        teamBPlayers: List<String>,
        maxOvers: Int,
        maxWickets: Int,
        ballType: BallType,
        matchType: MatchType,
        tossWinner: String,
        tossChoice: TossChoice,
        striker: String,
        nonStriker: String,
        bowler: String
    ) -> Unit
) {
    var teamAName by remember { mutableStateOf("Gully Kings") }
    var teamBName by remember { mutableStateOf("Street Strikers") }

    var selectedOvers by remember { mutableIntStateOf(10) }
    var selectedWickets by remember { mutableIntStateOf(10) }

    var selectedBallType by remember { mutableStateOf(BallType.TENNIS) }
    var selectedMatchType by remember { mutableStateOf(MatchType.FRIENDLY) }

    // Player Names
    var teamAPlayer1 by remember { mutableStateOf("Rohit (C)") }
    var teamAPlayer2 by remember { mutableStateOf("Virat") }
    var teamAPlayer3 by remember { mutableStateOf("KLR") }

    var teamBPlayer1 by remember { mutableStateOf("Bumrah (C)") }
    var teamBPlayer2 by remember { mutableStateOf("Shami") }
    var teamBPlayer3 by remember { mutableStateOf("Siraj") }

    // Toss Simulation
    var userTossChoice by remember { mutableStateOf(TossOption.HEADS) }
    var tossResult by remember { mutableStateOf<String?>(null) }
    var tossWinnerTeam by remember { mutableStateOf("Gully Kings") }
    var tossDecision by remember { mutableStateOf(TossChoice.BAT) }

    // Initial Match Selection
    var selectedStriker by remember { mutableStateOf("Rohit (C)") }
    var selectedNonStriker by remember { mutableStateOf("Virat") }
    var selectedBowler by remember { mutableStateOf("Bumrah (C)") }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("NEW MATCH SETUP", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Teams Section
                GlassCard {
                    Text("TEAMS", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = teamAName,
                        onValueChange = { teamAName = it },
                        label = { Text("Team A Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teamBName,
                        onValueChange = { teamBName = it },
                        label = { Text("Team B Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Overs & Wickets
                GlassCard {
                    Text("MATCH FORMAT", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Overs: $selectedOvers", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(5, 8, 10, 12, 15, 20).forEach { overs ->
                            FilterChip(
                                selected = selectedOvers == overs,
                                onClick = { selectedOvers = overs },
                                label = { Text("$overs Overs") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Wickets: $selectedWickets", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(5, 8, 10).forEach { w ->
                            FilterChip(
                                selected = selectedWickets == w,
                                onClick = { selectedWickets = w },
                                label = { Text("$w Wickets") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Ball Type & Match Type
                GlassCard {
                    Text("BALL & MATCH TYPE", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Ball Type", color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        BallType.entries.forEach { ball ->
                            FilterChip(
                                selected = selectedBallType == ball,
                                onClick = { selectedBallType = ball },
                                label = { Text(ball.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Match Type", color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        MatchType.entries.forEach { match ->
                            FilterChip(
                                selected = selectedMatchType == match,
                                onClick = { selectedMatchType = match },
                                label = { Text(match.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // Toss Simulation
                GlassCard {
                    Text("COIN TOSS", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Call Choice:", color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = userTossChoice == TossOption.HEADS,
                                onClick = { userTossChoice = TossOption.HEADS },
                                label = { Text("HEADS") }
                            )
                            FilterChip(
                                selected = userTossChoice == TossOption.TAILS,
                                onClick = { userTossChoice = TossOption.TAILS },
                                label = { Text("TAILS") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val outcome = if (Random.nextBoolean()) TossOption.HEADS else TossOption.TAILS
                            val won = userTossChoice == outcome
                            tossResult = "Flipped $outcome! ${if (won) "$teamAName Won Toss!" else "$teamBName Won Toss!"}"
                            tossWinnerTeam = if (won) teamAName else teamBName
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FLIP COIN")
                    }

                    tossResult?.let { res ->
                        Text(
                            text = res,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Winner Chooses:", color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = tossDecision == TossChoice.BAT,
                                    onClick = { tossDecision = TossChoice.BAT },
                                    label = { Text("BAT FIRST") }
                                )
                                FilterChip(
                                    selected = tossDecision == TossChoice.BOWL,
                                    onClick = { tossDecision = TossChoice.BOWL },
                                    label = { Text("BOWL FIRST") }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Continue to Calibration Button
            Button(
                onClick = {
                    val teamAPlayers = listOf(teamAPlayer1, teamAPlayer2, teamAPlayer3, "Player A4", "Player A5", "Player A6")
                    val teamBPlayers = listOf(teamBPlayer1, teamBPlayer2, teamBPlayer3, "Player B4", "Player B5", "Player B6")

                    onSaveSetup(
                        teamAName,
                        teamBName,
                        teamAPlayers,
                        teamBPlayers,
                        selectedOvers,
                        selectedWickets,
                        selectedBallType,
                        selectedMatchType,
                        tossWinnerTeam,
                        tossDecision,
                        selectedStriker,
                        selectedNonStriker,
                        selectedBowler
                    )
                    onNavigateCalibration()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(54.dp)
            ) {
                Icon(Icons.Default.SportsCricket, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("PROCEED TO AR CALIBRATION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
