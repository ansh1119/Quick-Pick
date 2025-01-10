package com.example.quickpick.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quickpick.models.Order
import com.example.quickpick.utils.ResultState
import com.example.quickpick.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun OrdersScreen(
    outletId: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()

    // Fetch orders when the screen is loaded
    LaunchedEffect(outletId) {
        viewModel.fetchOrders(outletId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = ordersState) {
            is ResultState.Loading -> CircularProgressIndicator()

            is ResultState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No orders found.")
                } else {
                    LazyColumn {
                        items(state.data) { order ->
                            OrderCard(order)
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
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val formatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
    val formattedTimestamp = remember(order.timestamp) {
        formatter.format(order.timestamp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Price: ₹${order.totalPrice}")
            Text("Time: $formattedTimestamp")
            Text("Items:")
            order.items.forEach { cartItem ->
                Text("- ${cartItem.item?.name} (x${cartItem.quantity})")
            }
        }
    }
}