package com.bettyai.assistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bettyai.assistant.ui.conversation.ConversationScreen
import com.bettyai.assistant.ui.home.HomeScreen
import com.bettyai.assistant.ui.permissions.PermissionsScreen
import com.bettyai.assistant.ui.settings.SettingsScreen

/** All navigation routes in one place. */
object BettyRoutes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
    const val CONVERSATION = "conversation/{conversationId}"

    fun conversation(id: Long) = "conversation/$id"
}

@Composable
fun BettyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = BettyRoutes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(BettyRoutes.HOME) {
            HomeScreen(
                onOpenConversation = { id -> navController.navigate(BettyRoutes.conversation(id)) },
                onOpenSettings = { navController.navigate(BettyRoutes.SETTINGS) },
                onOpenPermissions = { navController.navigate(BettyRoutes.PERMISSIONS) }
            )
        }

        composable(
            route = BettyRoutes.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ConversationScreen(onBack = { navController.popBackStack() })
        }

        composable(BettyRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPermissions = { navController.navigate(BettyRoutes.PERMISSIONS) }
            )
        }

        composable(BettyRoutes.PERMISSIONS) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
    }
}
