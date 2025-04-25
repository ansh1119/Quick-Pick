package com.founders.quickpick.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.founders.quickpick.components.AppButton
import com.founders.quickpick.components.BottomNav
import com.founders.quickpick.components.CommonDialog
import com.founders.quickpick.components.InputField
import com.founders.quickpick.getOutletId
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel

@Composable
fun AddItemsScreen(navController: NavController,viewModel: AuthViewModel= hiltViewModel()) {

    var name by remember{
        mutableStateOf("")
    }
    var category by remember{
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

    val getImage =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // This block is triggered when an image is picked
            imageUri = uri
        }

    if(isDialog){
        CommonDialog()
    }
    Scaffold(
        bottomBar = { BottomNav(navController = navController)}
    ) {innerPadding->



        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            Text(text = "Item Name",
                modifier=Modifier.padding(start = 80.dp))
            InputField(
                value = name,
                onValueChange = {name=it},
                placeholder = "E.g. Samosa",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                keyboardOptions = KeyboardOptions()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Item Category",
                modifier=Modifier.padding(start = 80.dp))
            InputField(
                value = category,
                onValueChange = {category=it},
                placeholder = "E.g. Snacks",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                keyboardOptions = KeyboardOptions()
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Item Price",
                modifier=Modifier.padding(start = 80.dp))
            InputField(
                value = price,
                onValueChange = {price=it},
                placeholder = "E.g. 12",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Box(
                modifier = Modifier
                    .padding(start = 80.dp, top = 20.dp)
                    .size(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = "Business Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    IconButton(onClick = { getImage.launch("image/*") }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_camera),
                            contentDescription = "Upload Logo",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, shape = RoundedCornerShape(50))
                                .padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            // Submit button
            AppButton(onButtonClick = {
                if (name.isNotEmpty() && price.isNotEmpty()) {
                    viewModel.addMenuItem(context,outletId.toString(), name, price.toDouble(), imageUri,category)
                }
                else{
                    Toast.makeText(context,"Name or price missing", Toast.LENGTH_SHORT).show()
                }
            },
                modifier= Modifier
                    .fillMaxWidth(.8f)
                    .align(Alignment.CenterHorizontally),
                content = "Add Menu Item")


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