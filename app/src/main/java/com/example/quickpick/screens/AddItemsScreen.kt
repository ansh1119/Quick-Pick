package com.example.quickpick.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quickpick.components.CommonDialog
import com.example.quickpick.components.UnderlinedTextField
import com.example.quickpick.getOutletId
import com.example.quickpick.utils.ResultState
import com.example.quickpick.viewmodel.AuthViewModel
import java.time.LocalDate

@Composable
fun AddItemsScreen(navController: NavController,viewModel: AuthViewModel= hiltViewModel()) {

    var name by remember{
        mutableStateOf("")
    }

    var isDialog by remember {
        mutableStateOf(false)
    }

    var price by remember{
        mutableStateOf("")
    }
    val context=LocalContext.current
    val outletId= getOutletId(context)
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val state = viewModel.addMenuItemState.collectAsState()

    if(isDialog){
        CommonDialog()
    }
    Scaffold {innerPadding->



        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            // Example fields to add a menu item


            // Input fields for menu item details
            UnderlinedTextField(modifier = Modifier, label = "Name", value = name) {newValue->
                name=newValue
            }

            UnderlinedTextField(modifier = Modifier, label = "Price", value = price) {newValue->
                price=newValue
            }
            Button(onClick = { /* Open image picker and set imageUri */ }) {
                Text("Select Image")
            }

            // Submit button
            Button(onClick = {
                if (name.isNotEmpty() && price.isNotEmpty()) {
                    viewModel.addMenuItem(outletId.toString(), name, price.toDouble(), imageUri)
                }
            }) {
                Text("Add Menu Item")
            }

            // Handle state
            when (val result = state.value) {
                is ResultState.Loading -> isDialog=true
                is ResultState.Success -> {Text("Menu Item added successfully!")
                isDialog=false}
                is ResultState.Failure -> {Text("Error: ${result.msg}", color = Color.Red)
                    isDialog=false}
                ResultState.Idle -> isDialog=false // Do nothing
            }
        }
    }

}