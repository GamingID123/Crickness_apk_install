package com.example.ui.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MatchEngineState
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientBackground
import com.example.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    matchState: MatchEngineState,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("BATTING", "BOWLING", "SUMMARY")

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopAppBar(
                title = { Text("SCORECARD", color = NeonGreen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = NeonGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Batting
                        GlassCard {
                            Text("BATTING SCORECARD - ${matchState.battingTeam}", color = NeonGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            matchState.playerStatsMap.values.filter { it.ballsFaced > 0 || it.isOut }.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(p.dismissalInfo, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                    }
                                    Text(
                                        "${p.runs} (${p.ballsFaced}b) • 4s: ${p.fours} 6s: ${p.sixes} • SR: %.1f".format(p.strikeRate),
                                        color = NeonGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            }
                        }
                    }
                    1 -> { // Bowling
                        GlassCard {
                            Text("BOWLING SCORECARD - ${matchState.bowlingTeam}", color = NeonGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            matchState.playerStatsMap.values.filter { it.ballsBowled > 0 }.forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(b.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${b.wicketsTaken}-${b.runsConceded} (${b.oversBowled}ov) • Econ: %.1f".format(b.economyRate),
                                        color = NeonGreen,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            }
                        }
                    }
                    else -> { // Summary
                        GlassCard {
                            Text("MATCH SUMMARY", color = NeonGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Team A (${matchState.teamA}): ${if (matchState.battingTeam == matchState.teamA) matchState.currentRuns else matchState.innings1Runs} / ${if (matchState.battingTeam == matchState.teamA) matchState.currentWickets else matchState.innings1Wickets}", color = Color.White)
                            Text("Team B (${matchState.teamB}): ${if (matchState.battingTeam == matchState.teamB) matchState.currentRuns else matchState.innings1Runs} / ${if (matchState.battingTeam == matchState.teamB) matchState.currentWickets else matchState.innings1Wickets}", color = Color.White)

                            if (matchState.resultMessage.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("RESULT: ${matchState.resultMessage}", color = NeonGreen, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
