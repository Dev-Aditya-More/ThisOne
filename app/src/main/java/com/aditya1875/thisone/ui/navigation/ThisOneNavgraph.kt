// ─────────────────────────────────────────────
// ui/navigation/ThisOneNavGraph.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aditya1875.thisone.ui.detail.MemeDetailScreen
import com.aditya1875.thisone.ui.home.HomeScreen
import com.aditya1875.thisone.ui.saved.SavedScreen
import com.aditya1875.thisone.ui.saved.SavedViewModel
import org.koin.androidx.compose.koinViewModel

object Routes {
    const val HOME   = "home"
    const val SAVED  = "saved"
    const val DETAIL = "detail"
}

@Composable
fun ThisOneNavGraph(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Shared instance so the Saved tab badge and the Saved screen see the same data.
    val savedViewModel: SavedViewModel = koinViewModel()
    val savedMemes by savedViewModel.savedMemes.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute != Routes.DETAIL) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Routes.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = null,
                            )
                        },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SAVED,
                        onClick = {
                            navController.navigate(Routes.SAVED) {
                                popUpTo(Routes.HOME)
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (savedMemes.isNotEmpty()) {
                                        Badge { Text("${savedMemes.size}") }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == Routes.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                )
                            }
                        },
                        label = { Text("Saved") },
                    )
                }
            }
        },
    ) { outerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = outerPadding.calculateBottomPadding()),
        ) {

            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToDetail = { navController.navigate(Routes.DETAIL) },
                )
            }

            composable(Routes.SAVED) {
                SavedScreen(
                    viewModel = savedViewModel,
                    onNavigateToDetail = { navController.navigate(Routes.DETAIL) },
                )
            }

            composable(Routes.DETAIL) {
                MemeDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
