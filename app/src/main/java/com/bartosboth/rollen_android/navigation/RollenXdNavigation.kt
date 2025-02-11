package com.bartosboth.rollen_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bartosboth.rollen_android.screens.main.MainScreen


@Composable
fun RollenXdNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainScreen) {
        composable<MainScreen> {
            MainScreen(navController)
        }
    }
}
