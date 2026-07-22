package com.example.repository

import com.example.database.*
import kotlinx.coroutines.flow.Flow

class CricknessRepository(
    private val matchDao: MatchDao,
    private val ballDao: BallDao,
    private val playerDao: PlayerDao
) {
    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    val allPlayers: Flow<List<PlayerEntity>> = playerDao.getAllPlayers()

    suspend fun getMatchById(id: Long): MatchEntity? = matchDao.getMatchById(id)

    suspend fun saveMatch(match: MatchEntity): Long = matchDao.insertMatch(match)

    suspend fun updateMatch(match: MatchEntity) = matchDao.updateMatch(match)

    suspend fun deleteMatch(matchId: Long) {
        matchDao.deleteMatchById(matchId)
        ballDao.deleteBallsForMatch(matchId)
    }

    fun getBallsForMatch(matchId: Long): Flow<List<BallEntity>> = ballDao.getBallsForMatch(matchId)

    suspend fun recordBall(ball: BallEntity): Long = ballDao.insertBall(ball)

    suspend fun deleteBall(ballId: Long) = ballDao.deleteBallById(ballId)

    suspend fun savePlayerStats(player: PlayerEntity) = playerDao.insertOrUpdatePlayer(player)
}
