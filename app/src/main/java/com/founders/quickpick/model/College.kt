package com.founders.quickpick.model

import android.net.Uri

data class College(
    val id:String?="",
    val name:String?="",
    val logo:Uri?=null,
    val outlets:List<String>?= listOf()
)