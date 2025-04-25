package com.founders.quickpick.model

data class MenuItem(
    val outletId:String="",
    val id:String?="",
    val name:String?="",
    val price:Double?=0.0,
    val available:Boolean?=true,
    val image:String?=null,
    val category: String?=""
)