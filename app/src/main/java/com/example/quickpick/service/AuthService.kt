package com.example.quickpick.service

import android.app.Activity
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
}
