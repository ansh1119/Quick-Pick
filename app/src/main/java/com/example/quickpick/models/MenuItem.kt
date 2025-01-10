package com.example.quickpick.models

import android.net.Uri

data class MenuItem(
    val outletId: String = "",
    val id: String? = "",
    val name: String? = "",
    val price: Double = 0.0, // Changed to non-nullable with a default value
    val available: Boolean = true, // Changed to non-nullable with a default value
    val image: String? = null // Store image as a String (URI as string)
)
