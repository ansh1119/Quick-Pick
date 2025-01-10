package com.example.quickpick.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quickpick.getOutletId
import com.example.quickpick.screens.AddItemsScreen
import com.example.quickpick.screens.OrdersScreen
import com.example.quickpick.screens.auth.EnterDetailsScreen
import com.example.quickpick.screens.auth.LoginScreen
import com.example.quickpick.screens.auth.OTPScreen


@Composable
fun App() {
    val navController= rememberNavController()
    val context= LocalContext.current
    val outletId= getOutletId(context)
    Log.d("OUTLET ID",outletId.toString())

    NavHost(navController = navController, startDestination = if(outletId!=null) "orders" else "login") {
        composable("login"){
            LoginScreen(navController)
        }
        composable(route = "otp/{phoneNumber}") { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPScreen(phoneNumber, navController)
        }
        composable("details"){
            EnterDetailsScreen(navController)
        }
        composable("menu item"){
            AddItemsScreen(navController)
        }
        composable("orders"){
            OrdersScreen(outletId = outletId.toString())
        }
    }
}