package com.founders.quickpick.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.founders.quickpick.R
import com.founders.quickpick.components.AppButton
import com.founders.quickpick.components.CommonDialog
import com.founders.quickpick.components.InputField
import com.founders.quickpick.setPhone
import com.founders.quickpick.ui.theme.CustomTypography
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {

    var phoneNo by remember {
        mutableStateOf("")
    }

    var isDialog by remember {
        mutableStateOf(false)
    }
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as Activity

    if (isDialog) {
        CommonDialog()
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8F8F8)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Image(painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier=Modifier.scale(.7f))

        Text(
            text = "Welcome to\n" +
                    "QuickPick",
            style = CustomTypography.displayLarge,
            fontSize = 44.sp,
            color=Color(0xFF7d4a6a)
        )

        Text(
            modifier= Modifier
                .fillMaxWidth(.65f)
                .padding(top = 50.dp),
            text = "Enter your Details",
            fontSize = 16.sp,
            color = Color(0xFF7d4a6a)
        )

        InputField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Name",
            modifier = Modifier
                .fillMaxWidth(.7f)
                .padding(top = 8.dp),
            KeyboardOptions(keyboardType = KeyboardType.Text).copy(
                imeAction = ImeAction.Next
            )
        )

//        // Submit Button
//        Button(
//            onClick = {
//                navController.navigate("college/\$name")
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Submit")
//        }

        TextField(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(.7f)
                .align(Alignment.CenterHorizontally),
            value = phoneNo,
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F4F4),
                unfocusedContainerColor = Color(0xFFF4F4F4),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            onValueChange = { newValue ->
                if (newValue.length <= 10) {
                    phoneNo = newValue
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number // Numeric keyboard
            ).copy(
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Row {
                    Text(
                        text = "+91",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 7.dp),

                        style = androidx.compose.ui.text.TextStyle(color = Color.Gray)
                    )

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



        Text(
            text = "We will send you a 6-digit verification code",
            color=Color(0xFF7d4a6a),
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 37.dp, top = 6.dp)
        )

        AppButton(
            onButtonClick = {
                if (phoneNo.length != 10) {
                    Toast.makeText(context, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                    return@AppButton
                }
                if (name.isBlank()) {
                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@AppButton
                }

                Log.d("LOGIN_SCREEN", "Starting OTP process for: $phoneNo") // ✅ Debug Log

                scope.launch(Dispatchers.Main) {
                    viewModel.createUserWithPhone(phoneNo, context).collect { result ->
                        when (result) {
                            is ResultState.Success -> {
                                Log.d("LOGIN_SCREEN", "OTP sent successfully for: $phoneNo") // ✅ Debug Log
                                setPhone(context, phoneNo)
                                isDialog = false
                                Toast.makeText(context, result.data, Toast.LENGTH_LONG).show()

                                navController.navigate(route = "otp/$phoneNo/$name") // ✅ Ensure we are going to OTP screen
                            }
                            is ResultState.Failure -> {
                                isDialog = false
                                Log.e("LOGIN_SCREEN", "OTP request failed: ${result.msg}") // ✅ Debug Log
                                Toast.makeText(context, result.msg.toString(), Toast.LENGTH_LONG).show()
                            }
                            ResultState.Loading -> {
                                isDialog = true
                                Log.d("LOGIN_SCREEN", "Loading OTP...") // ✅ Debug Log
                            }
                            ResultState.Idle -> {}
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(top = 29.dp)
                .size(276.dp, 50.dp)
                .align(Alignment.CenterHorizontally),
            content = "Generate OTP"
        )

        Spacer(modifier = Modifier.weight(1f))
    }

}

