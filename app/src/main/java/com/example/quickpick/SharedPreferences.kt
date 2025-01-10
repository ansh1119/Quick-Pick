package com.example.quickpick

import android.content.Context

fun getOutletId(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString("outletId", null )
}

fun setOutletId(context: Context, outletId: String) {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("outletId", outletId).apply()
}
