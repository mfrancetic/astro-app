package com.udacity.astroapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SearchTextField(
    initialValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Search",
    placeholder: String = "",
    searchContentDescription: String = "Search",
    clearContentDescription: String = "Clear search",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var currentText by rememberSaveable { mutableStateOf(initialValue) }
    
    LaunchedEffect(initialValue) {
        currentText = initialValue
    }
    
    OutlinedTextField(
        value = currentText,
        onValueChange = { newValue ->
            currentText = newValue
            onValueChange(newValue)
        },
        modifier = modifier,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = searchContentDescription
            )
        },
        trailingIcon = {
            if (currentText.isNotBlank()) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = clearContentDescription,
                    modifier = Modifier.clickable {
                        currentText = ""
                        onValueChange("")
                    }
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}