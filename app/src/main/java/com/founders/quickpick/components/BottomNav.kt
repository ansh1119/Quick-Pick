package com.founders.quickpick.components


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavItem("available", Icons.Default.Home, "Home")
    object Search : NavItem("orders", Icons.Default.Notifications, "Manage Orders")
    object Profile : NavItem("dashboard", Icons.Default.ShoppingCart, "dashboard")
}


val bottomNavItems = listOf(
    NavItem.Home,
    NavItem.Search,
    NavItem.Profile,
)

@Composable
fun BottomNav(navController: NavController) {


    NavigationBar(
        modifier=Modifier
            .windowInsetsPadding(
            WindowInsets.safeContent.only(
                WindowInsetsSides.Bottom // Adjust for the bottom system bar
            )
        ),
        containerColor = Color(0xFFF8F8F8),
        tonalElevation = 5.dp
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        if(currentRoute.equals("available")){
                            navController.popBackStack(0,inclusive = true)
                        }
                        navController.navigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        tint = Color.Gray,
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label,
                        fontSize = 9.sp,
                        color = Color.Gray)
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = Color.Gray,
                    selectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFDFF0E6)
                )
            )
        }
    }
}



