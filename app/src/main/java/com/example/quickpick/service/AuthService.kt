package com.example.quickpick.service

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.example.quickpick.models.College
import com.example.quickpick.models.MenuItem
import com.example.quickpick.models.Order
import com.example.quickpick.utils.ResultState
import kotlinx.coroutines.flow.Flow

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
        collegeName: String
    )

    suspend fun getColleges(): List<College>
    suspend fun addMenuItem(outletId: String, name: String, price: Double, image: Uri?)
    fun getOrdersForOutlet(outletId: String): Flow<ResultState<List<Order>>>
    suspend fun fetchOrderDetails(orderId: String): Order?
}
