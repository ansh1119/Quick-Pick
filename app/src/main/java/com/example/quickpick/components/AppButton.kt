package com.example.quickpick.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(onButtonClick:()->Unit, modifier: Modifier, content:String) {

    Button(onClick = onButtonClick,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp
        ),
        modifier=modifier, // Space around the button
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF369BFF))) {
        Text(text = content)
    }
}