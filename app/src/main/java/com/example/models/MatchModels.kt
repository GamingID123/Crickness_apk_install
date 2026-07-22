package com.example.models

import androidx.annotation.Keep

@Keep
enum class MatchType {
    FRIENDLY, TOURNAMENT, PRACTICE
}

@Keep
enum class BallType {
    TENNIS, LEATHER, RUBBER, TAPE_BALL
}

@Keep
enum class TossOption {
    HEADS, TAILS
}

@Keep
enum class TossChoice {
    BAT, BOWL
}

@Keep
enum class ExtraType {
    NONE, WIDE, NO_BALL, BYE, LEG_BYE
}

@Keep
enum class WicketType {
    NONE, BOWLED, CAUGHT, RUN_OUT, LBW, HIT_WICKET, STUMPED, RETIRED_HURT
}

@Keep
enum class InningsStatus {
    FIRST_INNINGS, INNINGS_BREAK, SECOND_INNINGS, COMPLETED
}

data class BallRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val overNumber: Int,
    val ballNumberInOver: Int,
    val runs: Int,
    val extraType: ExtraType = ExtraType.NONE,
    val extraRuns: Int = 0,
    val wicketType: WicketType = WicketType.NONE,
    val dismissedPlayerName: String? = null,
    val strikerName: String,
    val nonStrikerName: String,
    val bowlerName: String,
    val wagonWheelDegree: Float? = null, // 0 to 360 degrees for wagon wheel
    val isLegalBall: Boolean = true,
    val comment: String = ""
) {
    val totalRunsForBall: Int
        get() = runs + extraRuns + (if (extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) 1 else 0)
}

data class PlayerStats(
    val name: String,
    var runs: Int = 0,
    var ballsFaced: Int = 0,
    var fours: Int = 0,
    var sixes: Int = 0,
    var isOut: Boolean = false,
    var dismissalInfo: String = "not out",
    var oversBowled: Double = 0.0,
    var ballsBowled: Int = 0,
    var runsConceded: Int = 0,
    var wicketsTaken: Int = 0,
    var maidens: Int = 0
) {
    val strikeRate: Float
        get() = if (ballsFaced > 0) (runs.toFloat() / ballsFaced) * 100f else 0f

    val economyRate: Float
        get() {
            val totalOvers = (ballsBowled / 6) + ((ballsBowled % 6) / 6.0f)
            return if (totalOvers > 0) runsConceded / totalOvers else 0f
        }
}
