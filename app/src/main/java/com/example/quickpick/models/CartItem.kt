package com.example.quickpick.models

data class CartItem(
    val item: MenuItem? = null, // MenuItem should also have a no-argument constructor
    val quantity: Int = 0       // Default quantity
)
