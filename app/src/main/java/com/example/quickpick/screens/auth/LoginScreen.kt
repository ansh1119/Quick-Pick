package com.example.quickpick.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quickpick.components.AppButton
import com.example.quickpick.components.CommonDialog
import com.example.quickpick.utils.ResultState
import com.example.quickpick.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {

    var phoneNo by remember {
        mutableStateOf("")
    }

    var isDialog by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current as Activity

    if (isDialog) {
        CommonDialog()
    }


    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Enter your Phone Number",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 37.dp, top = 44.dp)
        )

        Text(
            text = "We will send you a 6-digit verification code",
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 37.dp, top = 6.dp)
        )

        OutlinedTextField(modifier = Modifier
            .padding(start = 37.dp, end = 37.dp, top = 16.dp)
            .size(321.dp, 55.dp)
            .align(Alignment.CenterHorizontally),
            value = phoneNo,
            singleLine = true,
            onValueChange = { newValue ->
                if (newValue.length <= 10) {
                    phoneNo = newValue
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number // Numeric keyboard
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

        AppButton(
            onButtonClick = {
                scope.launch(Dispatchers.Main) {
                    viewModel.createUserWithPhone(phoneNo,context).collect {
                        when (it) {
                            is ResultState.Success -> {
                                isDialog = false
                                Toast.makeText(context, it.data, Toast.LENGTH_LONG).show()
                                navController.navigate(route = "otp/$phoneNo")
                            }

                            is ResultState.Failure -> {
                                isDialog = false
                                Toast.makeText(context, it.msg.toString(), Toast.LENGTH_LONG).show()
                            }

                            ResultState.Loading -> {
                                isDialog = true
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

    }
}