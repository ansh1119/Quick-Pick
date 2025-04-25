package com.founders.quickpick.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun InputField(value:String, onValueChange:(String)->Unit,placeholder:String,modifier:Modifier,keyboardOptions: KeyboardOptions) {
    TextField(value = value,
        modifier=modifier,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF4F4F4),
            focusedContainerColor = Color(0xFFF4F4F4),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        placeholder = { Text(text = placeholder)},
        onValueChange = onValueChange)
}