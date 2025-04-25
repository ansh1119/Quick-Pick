package com.founders.quickpick.screens

import DatePickerField
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.founders.quickpick.components.BottomNav
import com.founders.quickpick.components.OrderCard
import com.founders.quickpick.getOutletId
import com.founders.quickpick.model.Order
import com.founders.quickpick.ui.theme.CustomTypography
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick.viewmodel.AuthViewModel
import java.util.Calendar
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val outletId = getOutletId(context)

    var fromDate by remember {
        mutableStateOf(getStartOfToday())
    }
    var toDate by remember {
        mutableStateOf(getEndOfToday())
    }

    var count by remember {
        mutableStateOf(0)
    }

    var amount by remember {
        mutableStateOf(0.0)
    }

//    var duration by remember{
//        mutableStateOf("Today")
//    }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }

    val ordersState by viewModel.orders.observeAsState(ResultState.Idle)
    val errorMessage by viewModel.errorMessage.observeAsState()

    // Handle errors during export
    exportErrorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(navController.context, errorMessage, Toast.LENGTH_SHORT).show()
            exportErrorMessage = null
        }
    }

    LaunchedEffect(fromDate, toDate) {
        viewModel.getOrdersForOutletInDateRange(
            outletId = outletId!!,
            startDate = Date(fromDate),
            endDate = Date(toDate)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Dashboard",
                            style = CustomTypography.headlineLarge
                        )
                    }
                },
            )
        },
        bottomBar = { BottomNav(navController = navController) }
    ) { innerPadding ->


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {


                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "From")
                    Spacer(modifier = Modifier.height(10.dp))
                    DatePickerField(
                        Modifier,
                        onDateSelected = { dateMillis ->
                            fromDate = dateMillis
                        }
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "To")
                    Spacer(modifier = Modifier.height(10.dp))
                    DatePickerField(Modifier, onDateSelected = { dateMillis ->
                        toDate = dateMillis
                    })
                }
            }
//
//            Button(
//                onClick = {
//                    isExporting = true
//                    viewModel.getOrdersForOutletInDateRange(
//                        outletId = outletId!!,
//                        startDate = Date(fromDate),
//                        endDate = Date(toDate)
//                    )
//                },
//                modifier = Modifier
//                    .fillMaxWidth(.9f)
//                    .align(Alignment.CenterHorizontally)
//                    .padding(top = 16.dp)
//            ){
//                if (isExporting) {
//                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
//                } else {
//                    Text(text = "Export to Excel")
//                }
//            }

            Card(
                modifier = Modifier
                    .padding(top = 19.dp)
                    .fillMaxWidth(.9f)
                    .align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F1F3)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 9.dp, horizontal = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = "Amount: ₹${String.format("%.2f",amount)}")
                    Text(text = "Count: $count")
                }
            }

            Text(
                text = "Sales",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 14.dp),
                style = CustomTypography.headlineLarge
            )

            when (ordersState) {
                is ResultState.Idle -> Text("Waiting for user interaction...")
                is ResultState.Loading -> CircularProgressIndicator()
                is ResultState.Success -> {

                    val bills =
                        (ordersState as ResultState.Success<List<Order>>).data

                    amount=bills.sumOf { it.totalPrice }
                    count=bills.size
                    if (bills.isEmpty()) {
                        Text(
                            "No estimates found for the selected date range.",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(.9f)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            val sortedBills = bills.sortedByDescending { it.timestamp }
                            items(sortedBills) { bill ->
                                OrderCard(
                                    bill,
                                    onUpdateStatus = {}
                                )
                            }
                        }
                    }
                }

                is ResultState.Failure -> {
                    val error =
                        (ordersState as ResultState.Failure).toString()
                    Log.w("ERROR", error)
                    Text(
                        "Error: ${error}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

        }
    }
}


fun getEndOfToday(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

fun getStartOfToday(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 1)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}