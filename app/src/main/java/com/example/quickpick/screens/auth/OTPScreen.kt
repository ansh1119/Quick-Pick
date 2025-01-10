package com.example.quickpick.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quickpick.components.AppButton
import com.example.quickpick.components.CommonDialog
import com.example.quickpick.components.OTP_VIEW_TYPE_BORDER
import com.example.quickpick.components.OtpView
import com.example.quickpick.utils.ResultState
import com.example.quickpick.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun OTPScreen(phoneNumber:String,navController: NavController,viewModel:AuthViewModel= hiltViewModel()) {


    var otpValue by remember{
        mutableStateOf("")
    }

    var isDialog by remember {
        mutableStateOf(false)
    }
    val scope= rememberCoroutineScope()
    val context= LocalContext.current

    if(isDialog){
        CommonDialog()
    }
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 230.dp)
                    .align(Alignment.CenterHorizontally),
                text = "Enter the OTP received on your\n mobile number",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
            )

            OtpView(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 33.dp),
                otpText = otpValue,
                onOtpTextChange = { otpValue = it },
                type = OTP_VIEW_TYPE_BORDER,
                password = true,
                containerSize = 48.dp,
                passwordChar = "•",
                charColor = Color.Black
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 35.dp),
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall,
                text = "Didn’t receive the OTP? Click here to resend."
            )

            Spacer(modifier = Modifier.weight(0.8f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 88.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AppButton(
                    modifier = Modifier.size(296.dp, 39.dp),
                    onButtonClick = {
                        scope.launch {
                            viewModel.signInWithCredential(otpValue).collect { result ->
                                when (result) {
                                    is ResultState.Success -> {
                                        Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                        navController.navigate("details")
                                        isDialog=false
                                    }
                                    is ResultState.Failure -> {
                                        isDialog = false
                                        Toast.makeText(context, result.msg.toString(), Toast.LENGTH_LONG).show()
                                    }
                                    ResultState.Loading -> isDialog = true
                                    ResultState.Idle -> isDialog = false
                                }
                            }
                        }
                    },
                    content = "VERIFY"
                )
            }
        }
    }
}