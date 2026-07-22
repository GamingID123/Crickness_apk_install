package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.CricknessViewModel
import com.example.ui.calibration.CalibrationScreen
import com.example.ui.help.HelpScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.live.LiveCameraArScreen
import com.example.ui.live.LiveMatchScreen
import com.example.ui.scoreboard.SaveMatchScreen
import com.example.ui.scoreboard.ScoreboardScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.setup.SetupMatchScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.statistics.StatisticsScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val NEW_MATCH = "new_match"
    const val CALIBRATION = "calibration"
    const val LIVE_MATCH = "live_match"
    const val CAMERA_AR = "camera_ar"
    const val SCOREBOARD = "scoreboard"
    const val SAVE_MATCH = "save_match"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val HELP = "help"
}

@Composable
fun AppNavigation(
    viewModel: CricknessViewModel,
    navController: NavHostController = rememberNavController()
) {
    val matchState by viewModel.matchEngineState.collectAsState()
    val pastMatches by viewModel.pastMatches.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it } },
        exitTransition = { fadeOut(tween(300)) + slideOutHorizontally { -it } },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it } },
        popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally { it } }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateNewMatch = { navController.navigate(Routes.NEW_MATCH) },
                onNavigateHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateStats = { navController.navigate(Routes.STATISTICS) },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateHelp = { navController.navigate(Routes.HELP) }
            )
        }

        composable(Routes.NEW_MATCH) {
            SetupMatchScreen(
                onNavigateCalibration = { navController.navigate(Routes.CALIBRATION) },
                onNavigateBack = { navController.popBackStack() },
                onSaveSetup = { teamA, teamB, teamAPlayers, teamBPlayers, overs, wickets, ballType, matchType, tossWinner, tossChoice, striker, nonStriker, bowler ->
                    viewModel.startNewMatch(
                        teamA = teamA,
                        teamB = teamB,
                        teamAPlayers = teamAPlayers,
                        teamBPlayers = teamBPlayers,
                        maxOvers = overs,
                        maxWickets = wickets,
                        ballType = ballType,
                        matchType = matchType,
                        tossWinner = tossWinner,
                        tossChoice = tossChoice,
                        striker = striker,
                        nonStriker = nonStriker,
                        bowler = bowler
                    )
                }
            )
        }

        composable(Routes.CALIBRATION) {
            CalibrationScreen(
                onCalibrationComplete = {
                    navController.navigate(Routes.LIVE_MATCH) {
                        popUpTo(Routes.NEW_MATCH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIVE_MATCH) {
            LiveMatchScreen(
                matchState = matchState,
                onRecordBall = { runs, extraType, extraRuns, wicketType, dismissedPlayer, wagonDegree ->
                    viewModel.recordBall(runs, extraType, extraRuns, wicketType, dismissedPlayer, wagonDegree)
                },
                onUndo = { viewModel.undoLastBall() },
                onRedo = { viewModel.redoLastBall() },
                onSwapBatsmen = { viewModel.swapBatsmen() },
                onSetBowler = { viewModel.setBowler(it) },
                onSetStriker = { viewModel.setStriker(it) },
                onSetNonStriker = { viewModel.setNonStriker(it) },
                onStartSecondInnings = { striker, nonStriker, bowler ->
                    viewModel.startSecondInnings(striker, nonStriker, bowler)
                },
                onNavigateCameraAr = { navController.navigate(Routes.CAMERA_AR) },
                onNavigateScoreboard = { navController.navigate(Routes.SCOREBOARD) },
                onNavigateSaveMatch = { navController.navigate(Routes.SAVE_MATCH) },
                onNavigateBack = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
            )
        }

        composable(Routes.CAMERA_AR) {
            LiveCameraArScreen(
                matchState = matchState,
                onRecordBall = { runs, extraType, extraRuns, wicketType, dismissedPlayer, wagonDegree ->
                    viewModel.recordBall(runs, extraType, extraRuns, wicketType, dismissedPlayer, wagonDegree)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCOREBOARD) {
            ScoreboardScreen(
                matchState = matchState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SAVE_MATCH) {
            SaveMatchScreen(
                matchState = matchState,
                onSaveAndExit = { pom ->
                    viewModel.saveCompletedMatch(pom)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                matches = pastMatches,
                onDeleteMatch = { viewModel.deleteMatch(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                players = allPlayers,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = appSettings,
                onUpdateDarkTheme = { viewModel.updateDarkTheme(it) },
                onUpdateDynamicColors = { viewModel.updateDynamicColors(it) },
                onUpdateDefaultOvers = { viewModel.updateDefaultOvers(it) },
                onUpdateDefaultWickets = { viewModel.updateDefaultWickets(it) },
                onUpdateAutoSave = { viewModel.updateAutoSave(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
