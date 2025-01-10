package com.example.quickpick.models

data class Outlet(
    val id:String?="",
    val ownerPhone: String?="",
    val collegeName:String?="",
    val outletName:String?="",
    val menu:List<String>?=listOf()
)