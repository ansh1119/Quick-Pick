package com.founders.quickpick.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.founders.quickpick.model.CartItem
import com.founders.quickpick.model.MenuItem
import com.founders.quickpick.ui.theme.CustomTypography


@Composable
fun MenuItemCard(
    menuItem: MenuItem?,
    cartItem: CartItem?,
    onAddToCart: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8),
        ),
//        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier= Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = menuItem?.image, contentDescription = "")
            Column(modifier = Modifier.padding(16.dp)) {
                menuItem?.name?.let { Text(text = it, style = CustomTypography.headlineLarge) }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Price: ₹${menuItem?.price}",
                    style = CustomTypography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

            }

            if (cartItem == null || cartItem.quantity == 0) {
                // Show Add button if the item is not in the cart
                Button(onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp,Color(0xFF4A7D5E))) {
                    Text("Add",
                        color = Color(0xFF4A7D5E))
                }
            } else {
                // Show quantity controls if the item is in the cart
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecreaseQuantity) {
                        Icon(Icons.Default.Clear, contentDescription = "Decrease")
                    }
                    Text(
                        text = "${cartItem.quantity}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onIncreaseQuantity) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }


    }
}
