package com.example.quickpick.utils


sealed class ResultState<out T> {

    data class Success<out R>(val data: R) : ResultState<R>()

    data class Failure(val msg: Throwable) : ResultState<Nothing>()

    object Loading : ResultState<Nothing>()

    object Idle : ResultState<Nothing>() // Define Idle state here

}