package com.founders.quickpick.viewmodel

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.founders.quickpick.MainActivity
import com.founders.quickpick.R
import com.founders.quickpick.getOutletId
import com.founders.quickpick.model.College
import com.founders.quickpick.model.MenuItem
import com.founders.quickpick.model.Order
import com.founders.quickpick.model.User
import com.founders.quickpick.service.AuthService
import com.founders.quickpick.utils.ResultState
import com.founders.quickpick2.model.Outlet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val firestore: FirebaseFirestore,
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _collegesState = MutableStateFlow<ResultState<List<College>>>(ResultState.Loading)
    val collegesState: StateFlow<ResultState<List<College>>> = _collegesState.asStateFlow()

    fun fetchColleges() {
        viewModelScope.launch {
            try {
                val colleges = authService.getColleges()
                _collegesState.value = ResultState.Success(colleges)
            } catch (e: Exception) {
                _collegesState.value = ResultState.Failure(e)
            }
        }
    }


    fun createUserWithPhone(
        mobile: String,
        activity: Activity
    ) = authService.createUserWithPhone(mobile, activity)

    fun signInWithCredential(
        code: String
    ) = authService.signInWithCredential(code)

    fun processExcelFile(
        uri: Uri,
        context: Context,
        onMenuItemsReady: (List<MenuItem>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val menuItems = mutableListOf<MenuItem>()
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    // Iterate over rows and create MenuItem objects
                    for (row in sheet) {
                        if (row.rowNum == 0) continue // Skip header row

                        val name = row.getCell(0)?.stringCellValue.orEmpty()
                        val price = row.getCell(1)?.numericCellValue ?: 0.0
                        val available = row.getCell(2)?.booleanCellValue ?: true

                        val menuItem = MenuItem(
                            name = name,
                            price = price,
                            available = available
                        )
                        menuItems.add(menuItem)
                    }

                    workbook.close()
                    inputStream.close()
                }

                // Add each MenuItem to Firestore and update the outlet's menu
                val firestore = Firebase.firestore
                val userPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
                val userRef = firestore.collection("users").document(userPhone)

                userRef.get().addOnSuccessListener { userSnapshot ->
                    val user = userSnapshot.toObject(User::class.java)
                    val outletId = user?.outlet ?: return@addOnSuccessListener

                    val outletRef = firestore.collection("outlets").document(outletId)

                    val menuItemIds = mutableListOf<String>()
                    for (menuItem in menuItems) {
                        val menuItemRef = firestore.collection("menu_item").document()
                        val menuItemWithId = menuItem.copy(id = menuItemRef.id, outletId = outletId)

                        menuItemRef.set(menuItemWithId).addOnSuccessListener {
                            menuItemIds.add(menuItemRef.id)
                        }
                    }

                    outletRef.update("menu", menuItemIds).addOnSuccessListener {
                        // Notify the UI that the menu items are ready
                        onMenuItemsReady(menuItems)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors, e.g., show a Toast in the UI or log the error
            }
        }
    }


    // Submit details to Firestore
    private val _submitResult = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val submitResult: StateFlow<ResultState<Unit>> get() = _submitResult

    fun submitDetails(
        context: Context,
        userPhone: String,
        outletName: String,
        menuItems: List<MenuItem>,
        collegeName: String,
        razorpayId:String
    ) {
        viewModelScope.launch {
            _submitResult.value = ResultState.Loading // Emit loading state

            try {
                authService.submitDetails(context, userPhone, outletName, menuItems, collegeName,razorpayId)
                _submitResult.value = ResultState.Success(Unit) // Emit success state
            } catch (e: Exception) {
                _submitResult.value = ResultState.Failure(e) // Emit error state
            }
        }
    }



    fun resetSubmitResult() {
        _submitResult.value = ResultState.Idle // Reset state to Idle
    }

    private val _addMenuItemState = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val addMenuItemState: StateFlow<ResultState<Unit>> = _addMenuItemState

    /**
     * Adds a menu item to Firestore and updates the outlet's menu list.
     */
    fun addMenuItem(context: Context,outletId: String, name: String, price: Double, image: Uri?,category: String) {
        viewModelScope.launch {
            _addMenuItemState.value = ResultState.Loading

            try {
                authService.addMenuItem(context,outletId, name, price, image, category = category)
                _addMenuItemState.value = ResultState.Success(Unit)
            } catch (e: Exception) {
                _addMenuItemState.value = ResultState.Failure(e)
            }
        }
    }


    private val _ordersState = MutableStateFlow<ResultState<List<Order>>>(ResultState.Idle)
    val ordersState: StateFlow<ResultState<List<Order>>> = _ordersState

    fun fetchOrders(outletId: String) {
        viewModelScope.launch {
            authService.getOrdersForOutlet(outletId).collect { result ->
                _ordersState.value = result
            }
        }
    }

    suspend fun fetchOrderDetails(orderId: String): Order? {
        return authService.fetchOrderDetails(orderId)
    }


    fun updateOrderStatus(orderId: String, newStatus: String, userId: String) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        // Reference to the order
        val orderRef = db.collection("orders").document(orderId)

        // Reference to the user's document or notifications subcollection
        val userNotificationRef = db.collection("users").document(userId)

        // Update order status
        batch.update(orderRef, "status", newStatus)

        // Notify the user by updating their document or adding a notification
        val notificationData = mapOf(
            "orderId" to orderId,
            "status" to newStatus,
            "timestamp" to System.currentTimeMillis()
        )

        batch.set(userNotificationRef.collection("notifications").document(orderId), notificationData)

        // Commit batch
        batch.commit()
            .addOnSuccessListener {
                Log.d("OrderUpdate", "Order $orderId updated to $newStatus and user notified")
            }
            .addOnFailureListener { e ->
                Log.e("OrderUpdate", "Error updating order or notifying user: $e")
            }
    }


    private val _updateStatusResult = MutableLiveData<Result<Unit>>()
    val updateStatusResult: LiveData<Result<Unit>> get() = _updateStatusResult

    fun markOrderAsReady(orderId: String) {
        authService.markOrderAsReady(
            orderId = orderId,
            onSuccess = {
                _updateStatusResult.postValue(Result.success(Unit))
            },
            onFailure = { exception ->
                _updateStatusResult.postValue(Result.failure(exception))
            }
        )
    }



    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Function to fetch menu items for a given outlet
    fun fetchMenuItems(outletId: String) {
        viewModelScope.launch {
            try {
                val items = authService.getMenuItems(outletId)
                _menuItems.postValue(items)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching menu items: ${e.message}")
            }
        }
    }

    // Function to update the availability of a menu item
    fun updateMenuItemAvailability(menuItemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            try {
                authService.updateMenuItemAvailability(menuItemId, isAvailable)

                // Update the LiveData for menu items locally
                _menuItems.value = _menuItems.value?.map { menuItem ->
                    if (menuItem.id == menuItemId) menuItem.copy(available = isAvailable) else menuItem
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update menu item: ${e.message}"
            }
        }
    }


    private val _outletOpenState = mutableStateOf(false)
    val outletOpenState: State<Boolean> = _outletOpenState

    // Fetch outlet details (including the open field)
    fun fetchOutletDetails(outletId: String) {
        firestore.collection("outlets")
            .document(outletId)
            .get()
            .addOnSuccessListener { document ->
                val outlet = document.toObject(Outlet::class.java)
                _outletOpenState.value = outlet?.open == true
            }
    }



    // Update outlet's open state in Firestore
    fun updateOutletOpenState(isOpen: Boolean,context: Context) {
        val outletId = getOutletId(context) // Get the outlet ID from your data source
        firestore.collection("outlets")
            .document(outletId!!)
            .update("open", isOpen)
            .addOnSuccessListener {
                // Successfully updated, the UI should reflect the change automatically due to observers
                _outletOpenState.value = isOpen
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "Error updating open state: ", e)
            }
    }



    private val _orders = MutableLiveData<ResultState<List<Order>>>(ResultState.Idle)
    val orders: LiveData<ResultState<List<Order>>> = _orders

    // Function to fetch orders within a date range for a specific outlet
    fun getOrdersForOutletInDateRange(outletId: String, startDate: Date, endDate: Date) {
        _orders.value = ResultState.Loading // Set state to Loading when fetching starts

        viewModelScope.launch {
            try {
                val ordersList = authService.getOrdersForOutletInDateRange(outletId, startDate, endDate)
                _orders.value = ResultState.Success(ordersList) // Set state to Success with the fetched orders
            } catch (e: Exception) {
                _orders.value = ResultState.Failure(e) // Set state to Failure if an error occurs
            }
        }
    }


    // LiveData to hold the Outlet data
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // Function to fetch the outlet by document ID
    fun fetchOutlet(phoneNumber: String) {
        viewModelScope.launch {
            try {
                Log.d("FETCH_OUTLET", "Fetching outlet for: $phoneNumber")

                val document = FirebaseFirestore.getInstance().collection("users")
                    .document(phoneNumber).get().await()

                if (document.exists()) {
                    val outletId = document.getString("outlet") ?: ""
                    Log.d("FETCH_OUTLET", "Outlet found: $outletId")

                    _user.postValue(User(outlet = outletId)) // ✅ Updating LiveData
                } else {
                    Log.d("FETCH_OUTLET", "No outlet found, user needs to register")
                    _user.postValue(User(outlet = "")) // ✅ Trigger recomposition
                }
            } catch (e: Exception) {
                Log.e("FETCH_OUTLET", "Error fetching outlet: ${e.message}")
            }
        }
    }





    private val appContext = application.applicationContext

    fun listenForNewOrders(outletId: String) {
        FirebaseFirestore.getInstance().collection("orders")
            .whereEqualTo("outletId", outletId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("OrderListener", "Error listening for orders", error)
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val newOrder = change.document.toObject(Order::class.java)
                        sendNotification(newOrder, appContext)
                    }
                }
            }
    }


    fun sendNotification(order: Order, context: Context) {
        val channelId = "order_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("orderId", order.id) // Pass orderId for navigation
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("New Order Received!")
            .setContentText("Order #${order.id} has been placed.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel) // Make sure this line is called!
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}

