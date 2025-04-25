package com.founders.quickpick.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun UnderlinedTextField(modifier:Modifier,label: String, value: String, onValueChange: (String) -> Unit) {
    val context= LocalContext.current

    Column(modifier=modifier) {
        // Label
        Text(
            text = label,
        )
        var keyboardOptions:KeyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Text)
        if(label.equals("Item Price")){
            keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Number)
        }


        var maxLength:Long

        TextField(
            value = value.replace('+',' '),
            keyboardOptions = keyboardOptions,
            onValueChange = onValueChange, // Removed curly braces for clarity
            modifier = Modifier.height(60.dp)
                .fillMaxWidth(.8f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Red
            )
        )
    }
}
