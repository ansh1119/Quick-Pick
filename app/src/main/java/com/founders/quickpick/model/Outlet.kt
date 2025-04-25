package com.founders.quickpick2.model

data class Outlet(
    val id: String? = "",
    val logo: String? = "",
    val ownerPhone: String? = "",
    val collegeName: String? = "",
    val outletName: String? = "",
    val menu: List<String>? = listOf(),
    val delivery: Boolean = false,
    val open: Boolean = false,
    val razorpayId: String="",
)