package com.example.quickpick.viewmodel

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickpick.models.College
import com.example.quickpick.models.MenuItem
import com.example.quickpick.models.Order
import com.example.quickpick.models.Outlet
import com.example.quickpick.models.User
import com.example.quickpick.service.AuthService
import com.example.quickpick.utils.ResultState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _colleges = MutableLiveData<List<College>>()
    val colleges: LiveData<List<College>> get() = _colleges

    init {
        // Fetch the colleges when the ViewModel is created
        fetchColleges()
    }

    private fun fetchColleges() {
        viewModelScope.launch {
            val collegeList = authService.getColleges()
            _colleges.postValue(collegeList)
        }
    }


    fun createUserWithPhone(
        mobile:String,
        activity: Activity
    )=authService.createUserWithPhone(mobile,activity)

    fun signInWithCredential(
        code:String
    )=authService.signInWithCredential(code)

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
        collegeName: String
    ) {
        viewModelScope.launch {
            _submitResult.value = ResultState.Loading // Emit loading state

            try {
                authService.submitDetails(context, userPhone, outletName, menuItems, collegeName)
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
    fun addMenuItem(outletId: String, name: String, price: Double, image: Uri?) {
        viewModelScope.launch {
            _addMenuItemState.value = ResultState.Loading

            try {
                authService.addMenuItem(outletId, name, price, image)
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

}

