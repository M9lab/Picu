package com.picu.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.picu.app.ui.chat.ChatScreen
import com.picu.app.ui.chatlist.ChatListScreen
import com.picu.app.ui.setup.RoleSetupScreen

object Routes {
    const val SETUP = "setup"
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{chatId}"
    fun chat(chatId: String) = "chat/$chatId"
}

@Composable
fun PicuNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SETUP) {
            RoleSetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.CHAT_LIST) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) }
            )
        }
        composable(Routes.CHAT) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
        }
    }
}
