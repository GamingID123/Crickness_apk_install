package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.MatchEntity
import com.example.database.PlayerEntity
import com.example.engine.MatchEngine
import com.example.models.BallType
import com.example.models.ExtraType
import com.example.models.MatchType
import com.example.models.TossChoice
import com.example.models.WicketType
import com.example.repository.AppSettings
import com.example.repository.CricknessRepository
import com.example.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CricknessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CricknessRepository(db.matchDao(), db.ballDao(), db.playerDao())
    val settingsRepository = SettingsRepository(application)

    val matchEngine = MatchEngine()
    val matchEngineState = matchEngine.state

    val appSettings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val pastMatches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.allPlayers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startNewMatch(
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
    ) {
        matchEngine.setupMatch(
            teamA = teamA,
            teamB = teamB,
            teamAPlayers = teamAPlayers,
            teamBPlayers = teamBPlayers,
            maxOvers = maxOvers,
            maxWickets = maxWickets,
            ballType = ballType,
            matchType = matchType,
            tossWinner = tossWinner,
            tossChoice = tossChoice,
            striker = striker,
            nonStriker = nonStriker,
            bowler = bowler
        )
    }

    fun recordBall(
        runs: Int,
        extraType: ExtraType = ExtraType.NONE,
        extraRuns: Int = 0,
        wicketType: WicketType = WicketType.NONE,
        dismissedPlayer: String? = null,
        wagonWheelDegree: Float? = null
    ) {
        matchEngine.recordBall(
            runs = runs,
            extraType = extraType,
            extraRuns = extraRuns,
            wicketType = wicketType,
            dismissedPlayer = dismissedPlayer,
            wagonWheelDegree = wagonWheelDegree
        )
    }

    fun undoLastBall() = matchEngine.undoLastBall()
    fun redoLastBall() = matchEngine.redoLastBall()
    fun swapBatsmen() = matchEngine.swapBatsmen()
    fun setBowler(name: String) = matchEngine.setBowler(name)
    fun setStriker(name: String) = matchEngine.setStriker(name)
    fun setNonStriker(name: String) = matchEngine.setNonStriker(name)
    fun startSecondInnings(striker: String, nonStriker: String, bowler: String) =
        matchEngine.startSecondInnings(striker, nonStriker, bowler)

    fun saveCompletedMatch(playerOfMatch: String) {
        val currState = matchEngineState.value
        viewModelScope.launch {
            val entity = MatchEntity(
                teamA = currState.teamA,
                teamB = currState.teamB,
                maxOvers = currState.maxOvers,
                maxWickets = currState.maxWickets,
                ballType = currState.ballType.name,
                matchType = currState.matchType.name,
                tossWinner = currState.tossWinner,
                tossChoice = currState.tossChoice.name,
                teamAScore = if (currState.battingTeam == currState.teamA) currState.currentRuns else currState.innings1Runs,
                teamAWickets = if (currState.battingTeam == currState.teamA) currState.currentWickets else currState.innings1Wickets,
                teamABalls = if (currState.battingTeam == currState.teamA) currState.legalBallsBowled else currState.innings1Balls,
                teamBScore = if (currState.battingTeam == currState.teamB) currState.currentRuns else currState.innings1Runs,
                teamBWickets = if (currState.battingTeam == currState.teamB) currState.currentWickets else currState.innings1Wickets,
                teamBBalls = if (currState.battingTeam == currState.teamB) currState.legalBallsBowled else currState.innings1Balls,
                target = currState.target ?: 0,
                winner = currState.winnerTeam,
                resultMessage = currState.resultMessage,
                playerOfTheMatch = playerOfMatch,
                isCompleted = true
            )
            repository.saveMatch(entity)

            // Save individual player stats update
            currState.playerStatsMap.values.forEach { stats ->
                val existing = repository.allPlayers.firstOrNull()?.find { it.name == stats.name }
                val updated = PlayerEntity(
                    name = stats.name,
                    matches = (existing?.matches ?: 0) + 1,
                    runs = (existing?.runs ?: 0) + stats.runs,
                    ballsFaced = (existing?.ballsFaced ?: 0) + stats.ballsFaced,
                    fours = (existing?.fours ?: 0) + stats.fours,
                    sixes = (existing?.sixes ?: 0) + stats.sixes,
                    wickets = (existing?.wickets ?: 0) + stats.wicketsTaken,
                    runsConceded = (existing?.runsConceded ?: 0) + stats.runsConceded,
                    ballsBowled = (existing?.ballsBowled ?: 0) + stats.ballsBowled,
                    highestScore = maxOf(existing?.highestScore ?: 0, stats.runs)
                )
                repository.savePlayerStats(updated)
            }
        }
    }

    fun deleteMatch(id: Long) {
        viewModelScope.launch {
            repository.deleteMatch(id)
        }
    }

    fun updateDarkTheme(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateDarkTheme(enabled) }
    fun updateDynamicColors(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateDynamicColors(enabled) }
    fun updateDefaultOvers(overs: Int) = viewModelScope.launch { settingsRepository.updateDefaultOvers(overs) }
    fun updateDefaultWickets(wickets: Int) = viewModelScope.launch { settingsRepository.updateDefaultWickets(wickets) }
    fun updateAutoSave(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAutoSave(enabled) }
}
