package com.founders.quickpick.model

data class Order(
    val logo:String?=null,
    val studentName:String?=null,
    val id: String? = null,
    val userId: String,
    val outletId: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Possible statuses: Pending, Completed, Cancelled
){
    constructor() : this(
        logo="",
        studentName="",
        id = null,
        userId = "",
        outletId = "",
        items = emptyList(),
        totalPrice = 0.0,
        timestamp = System.currentTimeMillis(),
        status = "Pending"
    )
}
