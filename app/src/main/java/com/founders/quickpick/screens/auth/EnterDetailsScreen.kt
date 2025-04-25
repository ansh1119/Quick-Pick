package com.founders.quickpick.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.founders.quickpick.R
import com.founders.quickpick.components.InputField
import com.founders.quickpick.model.College
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDetailsScreen(
    navController: NavController,
    name: String,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val collegesState = viewModel.collegesState.collectAsState()
    val context = LocalContext.current
    var razorpayId by remember {
        mutableStateOf("")
    }

    val submitResult by viewModel.submitResult.collectAsState()

    when (submitResult) {
        is ResultState.Idle -> {}
        is ResultState.Loading -> {
            // Show a loading indicator
            CircularProgressIndicator()
        }

        is ResultState.Success -> {
            // Navigate or show a success message
            Toast.makeText(context, "ban gya", Toast.LENGTH_SHORT).show()
            navController.popBackStack(0, inclusive = true)
            navController.navigate("available")
            viewModel.resetSubmitResult()
        }

        is ResultState.Failure -> {
            // Show an error message
            val errorMessage =
                (submitResult as ResultState.Failure).msg.toString() ?: "Unknown error"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
    var delivery by remember { mutableStateOf(false) }
    val userPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
    var selectedCollegeId by remember { mutableStateOf<String?>(null) }
    var selectedCollegeName by remember { mutableStateOf<String?>(null) }
    var outletName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Get the list of colleges from ResultState.Success
    val colleges = when (val state = collegesState.value) {
        is ResultState.Success -> state.data // Assume `data` is a list of colleges
        else -> emptyList() // In case of failure or loading
    }
    // Function to filter colleges by name
    val filteredColleges = remember(searchQuery) {
        colleges.filter {
            it.name?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    LaunchedEffect(colleges) {
        Log.d("College list", "CALL")
        viewModel.fetchColleges()
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "Enter Details")
        })
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(Color(0xFFF8F8F8))
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {
                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "",
                    Modifier
                        .size(250.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(3.dp))

                InputField(
                    value = outletName,
                    onValueChange = { newValue ->
                        outletName = newValue
                    },
                    placeholder = "Outlet Name",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f),
                    keyboardOptions = KeyboardOptions()
                )

                Spacer(modifier = Modifier.height(3.dp))
//
                InputField(
                    value = razorpayId,
                    onValueChange = { newValue ->
                        razorpayId = newValue
                    },
                    placeholder = "Razorpay Id",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f),
                    keyboardOptions = KeyboardOptions()
                )


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth(.8f)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Delivery Available")
                    Checkbox(checked = delivery, onCheckedChange = { delivery = !delivery })
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(.7f)
                        .align(Alignment.CenterHorizontally),
                    placeholder = { Text(text = "Search for Your College") },
                    value = searchQuery,
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F4F4),
                        unfocusedContainerColor = Color(0xFFF4F4F4),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    onValueChange = { newValue ->
                        searchQuery = newValue
                    },
                    leadingIcon = {
                        Row {
                            Icon(Icons.Default.Search, contentDescription = "")

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(Color.Black)
                            )
                        }
                    }
                )

                // College List
                when (val state = collegesState.value) {
                    is ResultState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ResultState.Success -> {
                        if (filteredColleges.isEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(state.data.size) { index ->
                                    val college = state.data[index]
                                    CollegeItem(
                                        college = college,
                                        isSelected = selectedCollegeId == college.id,
                                        onCollegeSelected = {
                                            selectedCollegeId = college.id
                                            selectedCollegeName = college.name
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredColleges.size) { index ->
                                    val college = filteredColleges[index]
                                    CollegeItem(
                                        college = college,
                                        isSelected = selectedCollegeId == college.id,
                                        onCollegeSelected = {
                                            selectedCollegeId = college.id
                                            selectedCollegeName = college.name
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is ResultState.Failure -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.msg.toString(), color = Color.Red)
                        }
                    }

                    ResultState.Idle -> {
                        // Handle Idle state if needed
                    }
                }
            }

            // Submit Button at the bottom
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {

                Button(
                    onClick = {
//                    setCollege(context, selectedCollegeId.toString())
                        if (selectedCollegeName != null) {
                            if (outletName != "") {
                                viewModel.submitDetails(
                                    context,
                                    userPhone,
                                    outletName,
                                    listOf(),
                                    selectedCollegeName.toString(),
                                    razorpayId
                                )
                            }

                        }

                    },
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(.7f)
                        .align(Alignment.CenterHorizontally), // Space around the button
                    enabled = selectedCollegeId != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7d4a6a)
                    )
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun CollegeItem(
    college: College,
    isSelected: Boolean,
    onCollegeSelected: (College) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onCollegeSelected(college) },
        elevation = CardDefaults.cardElevation(4.dp),
        border = if (isSelected) BorderStroke(1.dp, Color.Black) else BorderStroke(
            1.dp,
            Color(0xFF95F0B9)
        ),
        colors = CardDefaults.cardColors(if (isSelected) Color(0xFF7d4a6a) else Color.White)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color(0xFFFD7646),
                    clip = true,
                    spotColor = Color.White
                )
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = college.logo),
                contentDescription = "College Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = college.name ?: "Unknown College",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}
