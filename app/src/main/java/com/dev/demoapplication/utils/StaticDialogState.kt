package com.dev.demoapplication.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

object StaticDialogState {

    val isDialogVisible = mutableStateOf(false)  // Holds whether the dialog is visible or not
    var dialogTitle = mutableStateOf("Default Title")  // Holds the dialog title
    var dialogMessage = mutableStateOf("This is the default message.")  // Holds the dialog message
}


@Composable
fun StaticDialog() {
    if (StaticDialogState.isDialogVisible.value) {
        AlertDialog(
            onDismissRequest = {
                // Hide the dialog when dismissed
                StaticDialogState.isDialogVisible.value = false
            },
            title = {
                Text(text = StaticDialogState.dialogTitle.value)
            },
            text = {
                Text(text = StaticDialogState.dialogMessage.value)
            },
            confirmButton = {
                Button(onClick = {
                    // Close the dialog when "OK" is pressed
                    StaticDialogState.isDialogVisible.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ShowDialogButton() {
    Button(onClick = {
        // Set the dialog title and message dynamically when the button is clicked
        StaticDialogState.dialogTitle.value = "New Dialog Title"
        StaticDialogState.dialogMessage.value = "This is a dynamic message set by the button click."

        // Show the dialog
        StaticDialogState.isDialogVisible.value = true
    }) {
        Text("Show Dialog")
    }
}