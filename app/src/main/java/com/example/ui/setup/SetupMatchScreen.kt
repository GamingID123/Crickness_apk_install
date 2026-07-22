package com.example.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.BallType
import com.example.models.MatchType
import com.example.models.TossChoice
import com.example.models.TossOption
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
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
    var teamAName by remember { mutableStateOf("Team A") }
    var teamBName by remember { mutableStateOf("Team B") }

    var selectedOvers by remember { mutableIntStateOf(10) }
    var oversInputText by remember { mutableStateOf("10") }

    var selectedWickets by remember { mutableIntStateOf(10) }
    var wicketsInputText by remember { mutableStateOf("10") }

    var selectedBallType by remember { mutableStateOf(BallType.TENNIS) }
    var selectedMatchType by remember { mutableStateOf(MatchType.FRIENDLY) }

    // Custom non-preset player fields
    var strikerInput by remember { mutableStateOf("") }
    var nonStrikerInput by remember { mutableStateOf("") }
    var bowlerInput by remember { mutableStateOf("") }

    // Toss Simulation
    var userTossChoice by remember { mutableStateOf(TossOption.HEADS) }
    var tossResult by remember { mutableStateOf<String?>(null) }
    var tossWinnerTeam by remember { mutableStateOf("Team A") }
    var tossDecision by remember { mutableStateOf(TossChoice.BAT) }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Match Format: Overs (1 to 90) & Wickets (1 to 11)
                GlassCard {
                    Text("MATCH FORMAT (OVERS 1–90 • WICKETS 1–11)", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Overs Controls
                    Text("Overs: $selectedOvers (Range: 1 to 90)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedOvers > 1) {
                                    selectedOvers -= 1
                                    oversInputText = selectedOvers.toString()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Overs", tint = Color.White)
                        }

                        OutlinedTextField(
                            value = oversInputText,
                            onValueChange = { text ->
                                oversInputText = text
                                text.toIntOrNull()?.let { v ->
                                    if (v in 1..90) {
                                        selectedOvers = v
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        IconButton(
                            onClick = {
                                if (selectedOvers < 90) {
                                    selectedOvers += 1
                                    oversInputText = selectedOvers.toString()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Overs", tint = Color.White)
                        }
                    }

                    // Quick Overs Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(5, 10, 20, 50, 90).forEach { overs ->
                            FilterChip(
                                selected = selectedOvers == overs,
                                onClick = {
                                    selectedOvers = overs
                                    oversInputText = overs.toString()
                                },
                                label = { Text("$overs Ov") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wickets Controls
                    Text("Wickets: $selectedWickets (Range: 1 to 11)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedWickets > 1) {
                                    selectedWickets -= 1
                                    wicketsInputText = selectedWickets.toString()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Wickets", tint = Color.White)
                        }

                        OutlinedTextField(
                            value = wicketsInputText,
                            onValueChange = { text ->
                                wicketsInputText = text
                                text.toIntOrNull()?.let { v ->
                                    if (v in 1..11) {
                                        selectedWickets = v
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        IconButton(
                            onClick = {
                                if (selectedWickets < 11) {
                                    selectedWickets += 1
                                    wicketsInputText = selectedWickets.toString()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Wickets", tint = Color.White)
                        }
                    }

                    // Quick Wickets Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(1, 5, 8, 10, 11).forEach { w ->
                            FilterChip(
                                selected = selectedWickets == w,
                                onClick = {
                                    selectedWickets = w
                                    wicketsInputText = w.toString()
                                },
                                label = { Text("$w Wkt") },
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

                // Players Setup (No Presets)
                GlassCard {
                    Text("OPENING PLAYERS", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Type custom names for opening batsmen and bowler", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = strikerInput,
                        onValueChange = { strikerInput = it },
                        label = { Text("Striker Name", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g. Striker 1", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nonStrikerInput,
                        onValueChange = { nonStrikerInput = it },
                        label = { Text("Non-Striker Name", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g. Non-Striker 2", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bowlerInput,
                        onValueChange = { bowlerInput = it },
                        label = { Text("Opening Bowler Name", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g. Bowler 1", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.SportsCricket, contentDescription = null, tint = NeonGreen) },
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

                // Coin Toss
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
                            val nameA = teamAName.ifBlank { "Team A" }
                            val nameB = teamBName.ifBlank { "Team B" }
                            tossResult = "Flipped $outcome! ${if (won) "$nameA Won Toss!" else "$nameB Won Toss!"}"
                            tossWinnerTeam = if (won) nameA else nameB
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

            // Bottom Proceed Button
            Button(
                onClick = {
                    val finalTeamA = teamAName.ifBlank { "Team A" }
                    val finalTeamB = teamBName.ifBlank { "Team B" }

                    val finalStriker = strikerInput.ifBlank { "Striker 1" }
                    val finalNonStriker = nonStrikerInput.ifBlank { "Non-Striker 2" }
                    val finalBowler = bowlerInput.ifBlank { "Bowler 1" }

                    val teamAPlayers = listOf(finalStriker, finalNonStriker)
                    val teamBPlayers = listOf(finalBowler)

                    onSaveSetup(
                        finalTeamA,
                        finalTeamB,
                        teamAPlayers,
                        teamBPlayers,
                        selectedOvers.coerceIn(1, 90),
                        selectedWickets.coerceIn(1, 11),
                        selectedBallType,
                        selectedMatchType,
                        if (tossWinnerTeam.isBlank()) finalTeamA else tossWinnerTeam,
                        tossDecision,
                        finalStriker,
                        finalNonStriker,
                        finalBowler
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
