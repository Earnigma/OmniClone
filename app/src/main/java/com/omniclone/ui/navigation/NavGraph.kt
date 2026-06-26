package com.omniclone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.work.WorkManager
import com.omniclone.ui.clone.AppSelectorScreen
import com.omniclone.ui.clone.CloneConfigScreen
import com.omniclone.ui.clone.CloneDetailScreen
import com.omniclone.ui.clone.CloneManagerScreen
import com.omniclone.ui.clone.CloneProgressScreen
import com.omniclone.ui.features.AutomationBuilderScreen
import com.omniclone.ui.features.GpsSpoofScreen
import com.omniclone.ui.features.IdentityManagerScreen
import com.omniclone.ui.settings.HelpScreen
import com.omniclone.ui.settings.SettingsScreen

/**
 * Navigation graph for OmniClone.
 */
@Composable
fun OmniCloneNavGraph(
    navController: NavHostController,
    workManager: WorkManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "clone_manager",
        modifier = modifier
    ) {
        composable("clone_manager") {
            CloneManagerScreen(navController = navController)
        }
        composable("app_selector") {
            AppSelectorScreen(navController = navController)
        }
        composable("clone_config") {
            CloneConfigScreen(navController = navController)
        }
        composable("clone_progress") {
            CloneProgressScreen(navController = navController, workManager = workManager)
        }
        composable(
            "clone_detail/{cloneId}",
            arguments = listOf(navArgument("cloneId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cloneId = backStackEntry.arguments?.getString("cloneId") ?: ""
            CloneDetailScreen(cloneId = cloneId, navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("help") {
            HelpScreen(navController = navController)
        }
        composable("identity_manager") {
            IdentityManagerScreen(navController = navController)
        }
        composable("gps_spoof") {
            GpsSpoofScreen(navController = navController)
        }
        composable("automation_builder") {
            AutomationBuilderScreen(navController = navController)
        }
    }
}
