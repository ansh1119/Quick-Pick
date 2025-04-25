package com.founders.quickpick.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.founders.quickpick.components.AppButton
import com.founders.quickpick.components.CommonDialog
import com.founders.quickpick.components.OTP_VIEW_TYPE_BORDER
import com.founders.quickpick.components.OtpView
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@Composable
fun OTPScreen(phoneNumber:String, name:String, navController: NavController,viewModel:AuthViewModel= hiltViewModel()) {
    Log.d("PHONE",phoneNumber.toString())

    var otpValue by remember{
        mutableStateOf("")
    }


    val user = viewModel.user.observeAsState().value
    Log.d("OTP_SCREEN", "User state: $user") // ✅ Log observed changes

    var isDialog by remember {
        mutableStateOf(false)
    }
    val scope= rememberCoroutineScope()
    val context= LocalContext.current

    // 🔹 Use LaunchedEffect to react to `user` LiveData changes
    LaunchedEffect(key1 = user) {
        Log.d("OTP_SCREEN", "Observed user update: $user")

        if (user != null) {
            isDialog = false  // ✅ Hide loading dialog
            val outletId = user.outlet

            if (outletId.isNullOrBlank()) {
                Log.d("OTP_SCREEN", "No outlet found. Navigating to details.")
                navController.popBackStack(0, true)
                navController.navigate("details/$name")
            } else {
                Log.d("OTP_SCREEN", "Outlet found: $outletId. Navigating to available.")
                navController.popBackStack(0, true)
                navController.navigate("available")
            }
        }
    }

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
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        Log.d("OTP SUCCESS", currentUser.toString())
                                        Log.d("OTP SUCCESS", currentUser?.phoneNumber.toString())

                                        if (currentUser != null) {
                                            isDialog = true  // Show loader while fetching user
                                            viewModel.fetchOutlet("${currentUser.phoneNumber.toString()}")
                                            if(currentUser.phoneNumber!=null){
                                                navController.navigate("menu item")
                                            }
                                            else{
                                                navController.navigate("details/$name")
                                            }
                                        } else {
                                            // User not logged in, navigate to login
                                            isDialog = false
                                            navController.popBackStack(0, true)
                                            navController.navigate("details/$name")
                                        }
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


fun saveFcmTokenToFirestore(outletId: String, token: String) {
    FirebaseFirestore.getInstance().collection("outlets").document(outletId)
        .update("fcmToken", token)
        .addOnSuccessListener { Log.d("FCM", "Token saved") }
        .addOnFailureListener { Log.e("FCM", "Failed to save token", it) }
}
