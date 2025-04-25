package com.founders.quickpick.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.founders.quickpick.getOutletId
import com.founders.quickpick.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.founders.quickpick.components.AppButton
import com.founders.quickpick.components.BottomNav
import com.founders.quickpick.model.MenuItem
import com.founders.quickpick.ui.theme.CustomTypography
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun AvailabilityScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val menuItems by viewModel.menuItems.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState()
    val outletId = getOutletId(context)
    val outletOpenState by viewModel.outletOpenState // Observing the open state of the outlet

    var searchQuery by remember { mutableStateOf("") } // Search query state

    LaunchedEffect(outletId) {
        viewModel.fetchOutletDetails(outletId.toString())
        viewModel.fetchMenuItems(outletId.toString())
    }

    val bgColor = if (outletOpenState == true) {
        Color.White
    } else {
        Color.White
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("menu item") },
                containerColor = Color(0xFF7d4a6a)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                    tint = Color.White
                )
            }
        },
        bottomBar = { BottomNav(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(bgColor)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Add a switch to toggle the outlet open/close state
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Outlet Open",
                style = CustomTypography.displayLarge
            )
            Switch(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                checked = outletOpenState,
                onCheckedChange = { isOpen ->
                    viewModel.updateOutletOpenState(
                        isOpen,
                        context
                    ) // Update Firestore with the new state
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4A7D5E), // Green for checked state
                    checkedTrackColor = Color(0xFFD5D5D5), // Lighter green for track
                    uncheckedThumbColor = Color.Gray, // Gray for unchecked state
                    uncheckedTrackColor = Color.LightGray // Light gray for track
                )
            )

            // Add the search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("Search menu items") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (!errorMessage.isNullOrEmpty()) {
                    Text("Error: $errorMessage", color = Color.Red)
                }

                // Filter menu items based on the search query
                val filteredMenuItems = menuItems.filter {
                    it.name?.contains(searchQuery, ignoreCase = true) == true
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(filteredMenuItems) { menuItem ->
                        AvailableCard(navController,menuItem = menuItem, viewModel = viewModel)
                    }
                }
            }
        }
    }
}



@Composable
fun AvailableCard(navController: NavController,menuItem: MenuItem, viewModel: AuthViewModel) {
    var expanded by remember{
        mutableStateOf(false)
    }
    val outletOpenState by viewModel.outletOpenState
    // Define the card color based on availability
    val cardColor = if (menuItem.available == true) {
        Color(0xFFE8EEE9) // Existing color for available items
    } else {
        Color(0xFFFFE5E5) // Light red for unavailable items
    }
    val context= LocalContext.current
    val outletId= getOutletId(context)
    if(outletOpenState){
        OutlinedCard(
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp, horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
            ),
        ) {
            Column(modifier=Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = menuItem.image,
                        contentDescription = "",
                        modifier = Modifier.size(100.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        menuItem.name?.let { Text(text = it, style = CustomTypography.headlineLarge) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Price: ₹${menuItem.price}",
                            style = CustomTypography.bodyMedium
                        )
//                        Spacer(modifier = Modifier.height(10.dp))
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "")
                    }
                    Switch(
                        checked = menuItem.available == true,
                        onCheckedChange = {
                            viewModel.updateMenuItemAvailability(
                                menuItemId = menuItem.id.toString(),
                                isAvailable = it
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4A7D5E), // Green for checked state
                            checkedTrackColor = Color(0xFFf8f8f8), // Lighter green for track
                            uncheckedThumbColor = Color.Gray, // Gray for unchecked state
                            uncheckedTrackColor = Color.LightGray // Light gray for track
                        )
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier=Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedButton(
                        onClick = { deleteMenuItem(context,outletId.toString(),menuItem.id.toString(), onSuccess = {
                            navController.navigate("available")
                            Toast.makeText(
                                context,
                                "Item Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, onFailure = { Toast.makeText(context,"FAILED",Toast.LENGTH_SHORT).show() }) },
                        modifier = Modifier
                            .fillMaxWidth(.9f)
                            .align(Alignment.CenterHorizontally),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "",
                                tint = Color.Black)
                            Text(
                                text = "DELETE PRODUCT",
                                color = Color.Black,
                                style = CustomTypography.headlineSmall
                            )
                        }
                    }
                    val encodedUri = URLEncoder.encode(menuItem.image.toString(), StandardCharsets.UTF_8.toString())
                    AppButton(
                        onButtonClick = { navController.navigate("edit/" +
                                "${menuItem.name.toString()}/" +
                                "${menuItem.category.toString()}/" +
                                "${menuItem.price.toString()}/" +
                                "${menuItem.id.toString()}/" +
                                encodedUri
                        )},
                        modifier = Modifier.fillMaxWidth(.9f),
                        content = "Edit Product"
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

        }
    }
    else{
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFE5E5),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = menuItem.image,
                    contentDescription = "",
                    modifier = Modifier.size(100.dp)
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    menuItem.name?.let { Text(text = it, style = CustomTypography.headlineLarge) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Price: ₹${menuItem.price}",
                        style = CustomTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppButton(onButtonClick = { /*TODO*/ }, modifier = Modifier, content = "hello")
                }
                Switch(
                    checked = menuItem.available == true,
                    onCheckedChange = {
                       Toast.makeText(context,"The outlet is closed",Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4A7D5E), // Green for checked state
                        checkedTrackColor = Color(0xFFD5D5D5), // Lighter green for track
                        uncheckedThumbColor = Color.Gray, // Gray for unchecked state
                        uncheckedTrackColor = Color.LightGray // Light gray for track
                    )
                )
            }
        }
    }
}




fun deleteMenuItem(
    context: Context,
    outletId: String,
    itemId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Reference to the outlet document where the product ID needs to be removed
    val outletRef = db.collection("outlets").document(outletId)

    // Reference to the menu_items document that needs to be deleted
    val itemRef = db.collection("menu_items").document(itemId)

    // First, remove the product ID from the outlet's document
    outletRef.update("menu_items", FieldValue.arrayRemove(itemId))
        .addOnSuccessListener {
            // Now delete the product from the menu_items collection
            itemRef.delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
}
