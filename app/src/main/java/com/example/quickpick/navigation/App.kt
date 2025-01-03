package com.example.quickpick.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quickpick.screens.auth.LoginScreen
import com.example.quickpick.screens.auth.OTPScreen


@Composable
fun App() {
    val navController= rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login"){
            LoginScreen(navController)
        }
        composable(route = "otp/{phoneNumber}") { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPScreen(phoneNumber, navController)
        }
    }
}