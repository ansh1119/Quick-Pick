package com.example.quickpick.models

data class Order(
    val id: String? = null,           // Unique order ID
    val outletId: String? = null,     // Outlet ID the order belongs to
    val items: List<CartItem> = listOf(), // List of items in the order
    val totalPrice: Double = 0.0,     // Total price of the order
    val timestamp: Long = 0L,         // Time when the order was placed
    val status: String = "Pending",   // Order status (Pending, Completed, Cancelled)
    val userId: String? = null        // User ID (optional, for identifying the customer)
)
