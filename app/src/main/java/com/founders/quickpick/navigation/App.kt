package com.founders.quickpick.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.founders.quickpick.getOutletId
import com.founders.quickpick.screens.AddItemsScreen
import com.founders.quickpick.screens.AvailabilityScreen
import com.founders.quickpick.screens.Dashboard
import com.founders.quickpick.screens.EditProductScreen
import com.founders.quickpick.screens.OrdersScreen
import com.founders.quickpick.screens.auth.EnterDetailsScreen
import com.founders.quickpick.screens.auth.LoginScreen
import com.founders.quickpick.screens.auth.OTPScreen
import com.founders.quickpick.screens.auth.saveFcmTokenToFirestore
import com.founders.quickpick.setOutletId
import com.founders.quickpick.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


@Composable
fun App() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel()

    // Observing user details from Firestore using the ViewModel
    val user by viewModel.user.observeAsState()

    // This will hold the start destination for navigation
    var startDestination by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(currentUser) {
        Log.d("MainScreen", "Current Firebase User: ${currentUser?.phoneNumber.toString()}")

        // If there is no logged-in user
        if (currentUser == null) {
            Log.d("MainScreen", "No user logged in. Navigating to login.")
            startDestination = null
        } else {
            // Fetch the outlet data
            viewModel.fetchOutlet(currentUser.phoneNumber.toString())


        }
    }


    LaunchedEffect(user) {
        // Wait until user data is available in ViewModel
        // Check user data in Firestore
        if (user == null) {
            Log.d("MainScreen", "User document does not exist in Firestore. Navigating to login.")
            startDestination = "login"
        } else {
            Log.d("MainScreen", "User data: $user")
            val outletId = user?.outlet
            if (outletId.isNullOrBlank()) {
                Log.d("MainScreen", "No outlet found. Navigating to login.")
                startDestination = "login"
            } else {
                // Handle FirebaseMessaging token
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        saveFcmTokenToFirestore(outletId, token)
                    }
                }
                Log.d("MainScreen", "Outlet found: $outletId. Navigating to available.")
                setOutletId(context, outletId) // Cache the outlet ID locally if needed
                startDestination = "available"
            }
        }
    }

    // Check if startDestination is determined
    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("login") {
                LoginScreen(navController)
            }
            composable(route = "otp/{phoneNumber}/{name}") { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                OTPScreen(phoneNumber, name, navController)
            }
            composable("details/{name}") { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                EnterDetailsScreen(navController, name)
            }

            composable("menu item") {
                AddItemsScreen(navController)
            }
            composable("orders") {
                val outletId = getOutletId(context)
                OrdersScreen(navController, outletId = outletId.toString())
            }
            composable("available") {
                AvailabilityScreen(navController)
            }

            composable("dashboard") {
                Dashboard(navController = navController)
            }

            composable("edit/{name}/{category}/{price}/{id}/{uri}") {backStackEntry->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val price = backStackEntry.arguments?.getString("price") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val uri = backStackEntry.arguments?.getString("uri") ?: ""
                EditProductScreen(navController = navController,name,category,price,id,uri.toUri())
            }
        }
    } else {
        // Show a loading indicator while determining the destination
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}