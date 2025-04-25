package com.founders.quickpick.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.founders.quickpick.model.Order
import com.founders.quickpick.ui.theme.CustomTypography
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderCard(order: Order, onUpdateStatus: (String) -> Unit) {
    // SimpleDateFormat instance
    val formatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    var expanded by remember {
        mutableStateOf(false)
    }
    // Format the timestamp
    val formattedTimestamp = remember(order.timestamp) {
        formatter.format(order.timestamp)
    }


    // Define card colors based on order status
    val cardColor = when (order.status) {
        "Pending" -> Color(0xFFD3B5C8) // Default color for Pending
        "Ready" -> Color(0xFFD3B5C8) // Green for Completed
        "Picked" -> Color(0xFFD3B5C8) // Blue for Picked
        else -> Color(0xFFD3B5C8) // Default fallback color
    }

    // Order card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp,
                horizontal = 24.dp
            )
            .clickable { expanded = !expanded }, // Handle click event
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F1F3) // Highlight if selected
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier=Modifier.padding(horizontal = 16.dp)) {
                AsyncImage(model = order.logo, contentDescription = "")
                Column {
//                    Text(
//                        text = "Order ID: ${order.id ?: "N/A"}",
//                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
//                    )
                    Text(text = "Ordered by:- ${order.userId.substring(3)}",
                        style = CustomTypography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Price: ₹${order.totalPrice+order.totalPrice*0.01}",
                        style = CustomTypography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time: $formattedTimestamp",
                        style = CustomTypography.bodyLarge
                    )
                }
            }

            when (order.status) {
                "Pending" -> {
                    Button(
                        onClick = {
                            // Update order status to "Completed" or another status
                            onUpdateStatus("Completed")
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Mark as Completed")
                    }
                }

                "Completed" -> {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xff7d4a6a)
                        ),
                        onClick = {
                            // Change status to "Picked"
                            onUpdateStatus("Picked")
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Mark as Picked",
                            color = Color.White)
                    }
                }
                else->{
                    Text(
                        text = "Order is ${order.status}",
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Gray
                    )
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                if(!expanded)
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "")
            }
            // Display Order Details
            AnimatedVisibility(visible = expanded) {
                Column {
                    // Status of the Order
                    Spacer(modifier = Modifier.height(12.dp))

                    // List of Items in the Order
                    Text(
                        text = "Items:",
                        style = CustomTypography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    order.items.forEach { cartItem ->
                        Text(
                            text = "- ${cartItem.item?.name ?: "Unnamed Item"} (x${cartItem.quantity})",
                            style = CustomTypography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier=Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        if(expanded){
                            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "")
                        }
                    }
                    // Slide to Complete Section


                }

            }

        }
    }
}
