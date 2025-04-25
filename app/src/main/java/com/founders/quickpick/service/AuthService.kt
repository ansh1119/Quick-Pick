package com.founders.quickpick.service

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.founders.quickpick.model.College
import com.founders.quickpick.model.MenuItem
import com.founders.quickpick.model.Order
import com.founders.quickpick.model.User
import com.founders.quickpick.utils.ResultState
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface AuthService {

    fun createUserWithPhone(
        phone: String,
        activity:Activity
    ): Flow<ResultState<String>>

    fun signInWithCredential(
        otp: String
    ): Flow<ResultState<String>>

    suspend fun submitDetails(
        context: Context,
        userPhone: String,
        outletName: String,
        menuItems: List<MenuItem>,
        collegeName: String,
        razorpayId:String
    )

    suspend fun getColleges(): List<College>
    suspend fun addMenuItem(context: Context,outletId: String, name: String, price: Double, image: Uri?,category:String)
    fun getOrdersForOutlet(outletId: String): Flow<ResultState<List<Order>>>
    suspend fun fetchOrderDetails(orderId: String): Order?
    fun markOrderAsReady(orderId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun getMenuItems(outletId: String): List<MenuItem>
    suspend fun updateMenuItemAvailability(
        menuItemId: String, isAvailable: Boolean
    )

    suspend fun getOrdersForOutletInDateRange(
        outletId: String,
        startDate: Date,
        endDate: Date
    ): List<Order>

    suspend fun getOutlet(documentId: String): User?
}
