package com.founders.quickpick.model

data class CartItem(
    val item:MenuItem?=MenuItem(),
    val     quantity:Int?=0
)