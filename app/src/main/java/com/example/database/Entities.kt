package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.models.BallType
import com.example.models.ExtraType
import com.example.models.MatchType
import com.example.models.WicketType

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamA: String,
    val teamB: String,
    val maxOvers: Int,
    val maxWickets: Int,
    val ballType: String = BallType.TENNIS.name,
    val matchType: String = MatchType.FRIENDLY.name,
    val tossWinner: String = "",
    val tossChoice: String = "",
    val teamAScore: Int = 0,
    val teamAWickets: Int = 0,
    val teamABalls: Int = 0,
    val teamBScore: Int = 0,
    val teamBWickets: Int = 0,
    val teamBBalls: Int = 0,
    val target: Int = 0,
    val winner: String = "",
    val resultMessage: String = "",
    val playerOfTheMatch: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "balls")
data class BallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int, // 1 or 2
    val overNumber: Int,
    val ballNumberInOver: Int,
    val runs: Int,
    val extraType: String = ExtraType.NONE.name,
    val extraRuns: Int = 0,
    val wicketType: String = WicketType.NONE.name,
    val dismissedPlayerName: String? = null,
    val strikerName: String,
    val nonStrikerName: String,
    val bowlerName: String,
    val wagonWheelDegree: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_stats")
data class PlayerEntity(
    @PrimaryKey val name: String,
    val matches: Int = 0,
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val wickets: Int = 0,
    val runsConceded: Int = 0,
    val ballsBowled: Int = 0,
    val highestScore: Int = 0
)
