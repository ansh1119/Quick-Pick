import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    modifier: Modifier = Modifier,
    onDateSelected: (Long) -> Unit
) {
    val currentDateMillis = System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentDateMillis)
    val selectedDateMillis = datePickerState.selectedDateMillis ?: currentDateMillis
    val showDatePicker = remember { mutableStateOf(false) }

    TextField(
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color(0xFFF5F1F3), // Background color of the text field
            focusedIndicatorColor = Color.Transparent, // Removes the focused underline
            unfocusedIndicatorColor = Color.Transparent // Removes the unfocused underline
        ),
        value = convertMillisToDate(selectedDateMillis),
        onValueChange = { },
        trailingIcon = {
            IconButton(onClick = { showDatePicker.value = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        },
        readOnly = true,
        modifier = modifier
            .background(Color(0xFFF5F1F3))
            .fillMaxWidth()
            .clickable { showDatePicker.value = true } // Makes the entire text field clickable
    )

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis ?: currentDateMillis)
                    showDatePicker.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
