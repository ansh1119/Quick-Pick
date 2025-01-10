package com.example.quickpick.screens.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.quickpick.models.College
import com.example.quickpick.models.MenuItem
import com.example.quickpick.utils.ResultState
import com.example.quickpick.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun EnterDetailsScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val submitResult by viewModel.submitResult.collectAsState()

    val colleges by viewModel.colleges.observeAsState(emptyList())
    var outletName by remember { mutableStateOf("") }
    var selectedCollege by remember { mutableStateOf<College?>(null) }
    var newCollegeName by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf<List<MenuItem>>(listOf()) }
    val context = LocalContext.current
    val userPhone = Firebase.auth.currentUser?.phoneNumber.toString()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.processExcelFile(uri, context) { items ->
                menuItems = items
            }
        }
    }

    LaunchedEffect(submitResult) {
        when (submitResult) {
            is ResultState.Success -> {
                Toast.makeText(context, "Details submitted successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetSubmitResult() // Reset state after handling
                navController.navigate("orders")
            }
            is ResultState.Failure -> {
                val errorMessage = (submitResult as ResultState.Failure).msg.toString()
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // UI for entering outlet details, uploading Excel, and submitting
        OutlinedTextField(
            value = outletName,
            onValueChange = { outletName = it },
            label = { Text("Outlet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenu(
            expanded = selectedCollege != null,
            onDismissRequest = { selectedCollege = null }
        ) {
            colleges.forEach { college ->
                DropdownMenuItem(
                    onClick = { selectedCollege = college },
                    text = { Text(text = college.name.orEmpty()) }
                )
            }
        }

        if (selectedCollege == null) {
            OutlinedTextField(
                value = newCollegeName,
                onValueChange = { newCollegeName = it },
                label = { Text("New College Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("application/vnd.ms-excel") }) {
            Text("Upload Menu (Excel)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val collegeName = selectedCollege?.name ?: newCollegeName
                viewModel.submitDetails(context, userPhone, outletName, menuItems, collegeName)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}
