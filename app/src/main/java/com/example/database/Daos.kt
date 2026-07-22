package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: Long)
}

@Dao
interface BallDao {
    @Query("SELECT * FROM balls WHERE matchId = :matchId ORDER BY id ASC")
    fun getBallsForMatch(matchId: Long): Flow<List<BallEntity>>

    @Query("SELECT * FROM balls WHERE matchId = :matchId AND inningsNumber = :innings ORDER BY id ASC")
    fun getBallsForInnings(matchId: Long, innings: Int): Flow<List<BallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBall(ball: BallEntity): Long

    @Query("DELETE FROM balls WHERE id = :ballId")
    suspend fun deleteBallById(ballId: Long)

    @Query("DELETE FROM balls WHERE matchId = :matchId")
    suspend fun deleteBallsForMatch(matchId: Long)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_stats ORDER BY runs DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayer(player: PlayerEntity)

    @Query("SELECT * FROM player_stats WHERE name = :name")
    suspend fun getPlayerByName(name: String): PlayerEntity?
}
