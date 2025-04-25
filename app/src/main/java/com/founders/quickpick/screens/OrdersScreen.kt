package com.founders.quickpick.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.founders.quickpick.R
import com.founders.quickpick.components.BottomNav
import com.founders.quickpick.components.OrderCard
import com.founders.quickpick.ui.theme.CustomTypography

import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    navController: NavController,
    outletId: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()


    // Fetch orders when the screen is loaded
    LaunchedEffect(outletId) {
        viewModel.fetchOrders(outletId)
        viewModel.listenForNewOrders(outletId)
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xffF8F8F8)
            ),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Manage Orders")
                }
            }
        )
    }, bottomBar = { BottomNav(navController = navController) }) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            when (val state = ordersState) {
                is ResultState.Loading -> CircularProgressIndicator()

                is ResultState.Success -> {
                    if (state.data.isEmpty()) {
                        Column(modifier=Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(painter = painterResource(id = R.drawable.graylogo), contentDescription = "")
                            Text(text = "No orders yet!",
                                textAlign = TextAlign.Center,
                                color = Color(0xFFd0d0d0),
                                style = CustomTypography.displayLarge)
                        }
                    } else {
                        val sortedOrders = state.data.sortedByDescending { it.timestamp }

                            LazyColumn {
                                items(sortedOrders) { order ->
                                    if (order.status != "Picked") {
                                        OrderCard(order = order) { newStatus ->
                                            order.id?.let { orderId ->
                                                viewModel.markOrderAsReady(orderId)
                                            }
                                        }
                                    }
                                }
                            }


                    }
                }

                is ResultState.Failure -> Text(
                    text = "Error: ${state.msg}",
                    color = Color.Red
                )

                ResultState.Idle -> {
                    // Do nothing
                }

                else -> {}
            }
        }
    }

}
