package com.example.quickpick.models

import android.net.Uri

data class College(
    val id:String?="",
    val name:String?="",
    val logo:Uri?=null,
    val outlets:List<String>?= listOf()
)