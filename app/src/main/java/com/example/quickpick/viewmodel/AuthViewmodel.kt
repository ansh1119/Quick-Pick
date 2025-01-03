package com.example.quickpick.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickpick.service.AuthService
import com.example.quickpick.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    fun createUserWithPhone(
        mobile:String,
        activity: Activity
    )=authService.createUserWithPhone(mobile,activity)

    fun signInWithCredential(
        code:String
    )=authService.signInWithCredential(code)


}
