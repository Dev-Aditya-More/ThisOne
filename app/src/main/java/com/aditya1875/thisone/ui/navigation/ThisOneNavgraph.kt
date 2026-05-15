// ─────────────────────────────────────────────
// ui/navigation/ThisOneNavGraph.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aditya1875.thisone.ui.home.HomeScreen
import com.aditya1875.thisone.ui.saved.SavedScreen
import androidx.compose.ui.Modifier

object Routes {
    const val HOME  = "home"
    const val SAVED = "saved"
}

@Composable
fun ThisOneNavGraph(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToSaved = { navController.navigate(Routes.SAVED) }
            )
        }

        composable(Routes.SAVED) {
            SavedScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}