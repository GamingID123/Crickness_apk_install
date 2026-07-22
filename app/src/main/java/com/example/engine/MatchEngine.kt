package com.example.engine

import com.example.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MatchEngineState(
    val teamA: String = "Team A",
    val teamB: String = "Team B",
    val teamAPlayers: List<String> = listOf("Player A1", "Player A2", "Player A3", "Player A4", "Player A5", "Player A6"),
    val teamBPlayers: List<String> = listOf("Player B1", "Player B2", "Player B3", "Player B4", "Player B5", "Player B6"),
    val maxOvers: Int = 10,
    val maxWickets: Int = 10,
    val ballType: BallType = BallType.TENNIS,
    val matchType: MatchType = MatchType.FRIENDLY,
    val tossWinner: String = "Team A",
    val tossChoice: TossChoice = TossChoice.BAT,
    
    val currentInnings: Int = 1, // 1 or 2
    val battingTeam: String = "Team A",
    val bowlingTeam: String = "Team B",
    
    // Innings 1 Summary
    val innings1Runs: Int = 0,
    val innings1Wickets: Int = 0,
    val innings1Balls: Int = 0,
    
    // Current Innings Live Stats
    val currentRuns: Int = 0,
    val currentWickets: Int = 0,
    val legalBallsBowled: Int = 0, // e.g., 14 = 2.2 overs
    
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = "",
    
    val target: Int? = null,
    val inningsStatus: InningsStatus = InningsStatus.FIRST_INNINGS,
    val resultMessage: String = "",
    val winnerTeam: String = "",
    
    val ballHistory: List<BallRecord> = emptyList(),
    val currentOverBalls: List<BallRecord> = emptyList(),
    val undoStack: List<BallRecord> = emptyList(),
    val playerStatsMap: Map<String, PlayerStats> = emptyMap()
) {
    val oversFormatted: String
        get() {
            val overs = legalBallsBowled / 6
            val balls = legalBallsBowled % 6
            return "$overs.$balls"
        }

    val currentRunRate: Float
        get() {
            val overs = legalBallsBowled / 6.0f + (legalBallsBowled % 6) / 6.0f
            return if (overs > 0) currentRuns / overs else 0f
        }

    val requiredRunRate: Float?
        get() {
            if (target == null) return null
            val remainingRuns = target - currentRuns
            val remainingBalls = (maxOvers * 6) - legalBallsBowled
            val remainingOvers = remainingBalls / 6.0f
            return if (remainingOvers > 0) (remainingRuns / remainingOvers).coerceAtLeast(0f) else 0f
        }

    val projectedScore: Int
        get() {
            val crr = currentRunRate
            return if (crr > 0) (crr * maxOvers).toInt() else currentRuns
        }

    val partnershipRuns: Int
        get() {
            // sum runs scored since last wicket in current innings
            var sum = 0
            for (i in ballHistory.indices.reversed()) {
                val ball = ballHistory[i]
                if (ball.wicketType != WicketType.NONE) break
                sum += ball.totalRunsForBall
            }
            return sum
        }

    val partnershipBalls: Int
        get() {
            var balls = 0
            for (i in ballHistory.indices.reversed()) {
                val ball = ballHistory[i]
                if (ball.wicketType != WicketType.NONE) break
                if (ball.isLegalBall) balls++
            }
            return balls
        }
}

class MatchEngine {
    private val _state = MutableStateFlow(MatchEngineState())
    val state: StateFlow<MatchEngineState> = _state.asStateFlow()

    private val redoHistory = mutableListOf<BallRecord>()

    fun setupMatch(
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
        val batting = if ((tossWinner == teamA && tossChoice == TossChoice.BAT) ||
            (tossWinner == teamB && tossChoice == TossChoice.BOWL)
        ) teamA else teamB

        val bowling = if (batting == teamA) teamB else teamA

        val statsMap = mutableMapOf<String, PlayerStats>()
        (teamAPlayers + teamBPlayers).forEach { name ->
            statsMap[name] = PlayerStats(name = name)
        }

        _state.value = MatchEngineState(
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
            currentInnings = 1,
            battingTeam = batting,
            bowlingTeam = bowling,
            strikerName = striker,
            nonStrikerName = nonStriker,
            bowlerName = bowler,
            inningsStatus = InningsStatus.FIRST_INNINGS,
            playerStatsMap = statsMap
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
        val current = _state.value
        if (current.inningsStatus == InningsStatus.COMPLETED) return

        val isLegal = (extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL)
        val overNum = (current.legalBallsBowled / 6) + 1
        val ballNumInOver = if (isLegal) (current.legalBallsBowled % 6) + 1 else (current.currentOverBalls.filter { it.isLegalBall }.size + 1)

        val ball = BallRecord(
            overNumber = overNum,
            ballNumberInOver = ballNumInOver,
            runs = runs,
            extraType = extraType,
            extraRuns = extraRuns,
            wicketType = wicketType,
            dismissedPlayerName = dismissedPlayer ?: if (wicketType != WicketType.NONE) current.strikerName else null,
            strikerName = current.strikerName,
            nonStrikerName = current.nonStrikerName,
            bowlerName = current.bowlerName,
            wagonWheelDegree = wagonWheelDegree,
            isLegalBall = isLegal
        )

        applyBall(ball)
        redoHistory.clear()
    }

    private fun applyBall(ball: BallRecord) {
        val curr = _state.value
        val isLegal = ball.isLegalBall
        val newLegalBalls = if (isLegal) curr.legalBallsBowled + 1 else curr.legalBallsBowled
        val totalRunsForBall = ball.totalRunsForBall
        val newRuns = curr.currentRuns + totalRunsForBall
        val isWicket = ball.wicketType != WicketType.NONE
        val newWickets = if (isWicket) curr.currentWickets + 1 else curr.currentWickets

        // Update player stats
        val newStatsMap = curr.playerStatsMap.toMutableMap()

        // Striker stats
        val strikerStats = newStatsMap[ball.strikerName]?.copy() ?: PlayerStats(ball.strikerName)
        if (ball.extraType != ExtraType.WIDE) {
            strikerStats.ballsFaced += 1
            strikerStats.runs += ball.runs
            if (ball.runs == 4) strikerStats.fours += 1
            if (ball.runs == 6) strikerStats.sixes += 1
        }
        if (isWicket && ball.dismissedPlayerName == ball.strikerName) {
            strikerStats.isOut = true
            strikerStats.dismissalInfo = "b ${ball.bowlerName} (${ball.wicketType.name})"
        }
        newStatsMap[ball.strikerName] = strikerStats

        // Non-striker if dismissed
        if (isWicket && ball.dismissedPlayerName == ball.nonStrikerName) {
            val nonStrikerStats = newStatsMap[ball.nonStrikerName]?.copy() ?: PlayerStats(ball.nonStrikerName)
            nonStrikerStats.isOut = true
            nonStrikerStats.dismissalInfo = "run out"
            newStatsMap[ball.nonStrikerName] = nonStrikerStats
        }

        // Bowler stats
        val bowlerStats = newStatsMap[ball.bowlerName]?.copy() ?: PlayerStats(ball.bowlerName)
        if (isLegal) bowlerStats.ballsBowled += 1
        bowlerStats.runsConceded += totalRunsForBall
        if (isWicket && ball.wicketType != WicketType.RUN_OUT && ball.wicketType != WicketType.RETIRED_HURT) {
            bowlerStats.wicketsTaken += 1
        }
        bowlerStats.oversBowled = (bowlerStats.ballsBowled / 6) + ((bowlerStats.ballsBowled % 6) / 10.0)
        newStatsMap[ball.bowlerName] = bowlerStats

        val newHistory = curr.ballHistory + ball
        val isOverEnd = isLegal && (newLegalBalls % 6 == 0)
        val newCurrentOverBalls = if (isOverEnd) emptyList() else curr.currentOverBalls + ball

        // Determine Strike rotation
        var nextStriker = curr.strikerName
        var nextNonStriker = curr.nonStrikerName

        // Swap on odd runs
        if (ball.runs % 2 == 1) {
            val temp = nextStriker
            nextStriker = nextNonStriker
            nextNonStriker = temp
        }

        // Swap on over end
        if (isOverEnd) {
            val temp = nextStriker
            nextStriker = nextNonStriker
            nextNonStriker = temp
        }

        // Check Innings/Match end conditions
        var newInningsStatus = curr.inningsStatus
        var targetScore = curr.target
        var resultMsg = curr.resultMessage
        var winner = curr.winnerTeam
        var inn1Runs = curr.innings1Runs
        var inn1Wickets = curr.innings1Wickets
        var inn1Balls = curr.innings1Balls

        if (curr.currentInnings == 1) {
            val inningsEnded = newWickets >= curr.maxWickets || newLegalBalls >= curr.maxOvers * 6
            if (inningsEnded) {
                newInningsStatus = InningsStatus.INNINGS_BREAK
                inn1Runs = newRuns
                inn1Wickets = newWickets
                inn1Balls = newLegalBalls
                targetScore = newRuns + 1
            }
        } else { // Innings 2
            val target = curr.target ?: 0
            if (newRuns >= target) {
                newInningsStatus = InningsStatus.COMPLETED
                winner = curr.battingTeam
                val wicketsLeft = curr.maxWickets - newWickets
                resultMsg = "$winner won by $wicketsLeft wickets!"
            } else if (newWickets >= curr.maxWickets || newLegalBalls >= curr.maxOvers * 6) {
                newInningsStatus = InningsStatus.COMPLETED
                if (newRuns == target - 1) {
                    winner = "TIE"
                    resultMsg = "Match Tied! Super Over available."
                } else {
                    winner = curr.bowlingTeam
                    val runMargin = (target - 1) - newRuns
                    resultMsg = "$winner won by $runMargin runs!"
                }
            }
        }

        _state.value = curr.copy(
            currentRuns = newRuns,
            currentWickets = newWickets,
            legalBallsBowled = newLegalBalls,
            strikerName = nextStriker,
            nonStrikerName = nextNonStriker,
            ballHistory = newHistory,
            currentOverBalls = newCurrentOverBalls,
            playerStatsMap = newStatsMap,
            inningsStatus = newInningsStatus,
            target = targetScore,
            resultMessage = resultMsg,
            winnerTeam = winner,
            innings1Runs = inn1Runs,
            innings1Wickets = inn1Wickets,
            innings1Balls = inn1Balls
        )
    }

    fun startSecondInnings(striker: String, nonStriker: String, bowler: String) {
        val curr = _state.value
        val newBatting = curr.bowlingTeam
        val newBowling = curr.battingTeam

        _state.value = curr.copy(
            currentInnings = 2,
            battingTeam = newBatting,
            bowlingTeam = newBowling,
            currentRuns = 0,
            currentWickets = 0,
            legalBallsBowled = 0,
            strikerName = striker,
            nonStrikerName = nonStriker,
            bowlerName = bowler,
            currentOverBalls = emptyList(),
            inningsStatus = InningsStatus.SECOND_INNINGS
        )
    }

    fun undoLastBall() {
        val curr = _state.value
        if (curr.ballHistory.isEmpty()) return

        val lastBall = curr.ballHistory.last()
        redoHistory.add(lastBall)

        // Re-build state from scratch up to last ball - 1
        rebuildStateFromHistory(curr.ballHistory.dropLast(1))
    }

    fun redoLastBall() {
        if (redoHistory.isNotEmpty()) {
            val ball = redoHistory.removeAt(redoHistory.size - 1)
            applyBall(ball)
        }
    }

    fun swapBatsmen() {
        val curr = _state.value
        _state.value = curr.copy(
            strikerName = curr.nonStrikerName,
            nonStrikerName = curr.strikerName
        )
    }

    fun setBowler(name: String) {
        _state.value = _state.value.copy(bowlerName = name)
    }

    fun setStriker(name: String) {
        _state.value = _state.value.copy(strikerName = name)
    }

    fun setNonStriker(name: String) {
        _state.value = _state.value.copy(nonStrikerName = name)
    }

    private fun rebuildStateFromHistory(history: List<BallRecord>) {
        val curr = _state.value
        // Reset counters
        var runs = 0
        var wickets = 0
        var legalBalls = 0
        val statsMap = mutableMapOf<String, PlayerStats>()
        (curr.teamAPlayers + curr.teamBPlayers).forEach { statsMap[it] = PlayerStats(it) }

        var striker = curr.strikerName
        var nonStriker = curr.nonStrikerName
        val overBalls = mutableListOf<BallRecord>()

        history.forEach { ball ->
            val totalRuns = ball.totalRunsForBall
            runs += totalRuns
            if (ball.wicketType != WicketType.NONE) wickets++
            if (ball.isLegalBall) legalBalls++

            overBalls.add(ball)
            if (ball.isLegalBall && legalBalls % 6 == 0) overBalls.clear()
        }

        _state.value = curr.copy(
            currentRuns = runs,
            currentWickets = wickets,
            legalBallsBowled = legalBalls,
            ballHistory = history,
            currentOverBalls = overBalls,
            inningsStatus = if (curr.currentInnings == 1) InningsStatus.FIRST_INNINGS else InningsStatus.SECOND_INNINGS
        )
    }
}
