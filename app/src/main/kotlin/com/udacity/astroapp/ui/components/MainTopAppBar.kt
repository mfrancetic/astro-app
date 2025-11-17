package com.udacity.astroapp.ui.components

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    selectedDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    onDateSelected: ((LocalDate) -> Unit)? = null
) {
    val context = LocalContext.current

    TopAppBar(
        title = { Text(title) },
        actions = {
            if (onDateSelected != null) {
                IconButton(
                    onClick = {
                        // Use selectedDate if provided, otherwise use current date
                        val dateToShow = selectedDate ?: LocalDate.now()
                        val calendar = Calendar.getInstance()
                        calendar.set(
                            dateToShow.year,
                            dateToShow.monthValue - 1,
                            dateToShow.dayOfMonth
                        )

                        val dialog =
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newSelectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    onDateSelected(newSelectedDate)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )

                        // Set max date if provided
                        maxDate?.let {
                            val maxCalendar = Calendar.getInstance()
                            maxCalendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
                            dialog.datePicker.maxDate = maxCalendar.timeInMillis
                        }

                        dialog.show()
                    }
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.select_date)
                    )
                }
            }
        }
    )
}

// MainTopAppBar Previews
@Preview(name = "Top App Bar - Light", showBackground = true)
@Composable
private fun MainTopAppBarLightPreview() {
    AstroAppTheme(themePreference = 0) { MainTopAppBar(title = "Astronomy Photo") }
}

@Preview(
    name = "Top App Bar - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MainTopAppBarDarkPreview() {
    AstroAppTheme(themePreference = 1) { MainTopAppBar(title = "Astronomy Photo") }
}

@Preview(name = "Top App Bar with date picker - Light", showBackground = true)
@Composable
private fun MainTopAppBarWithDateLightPreview() {
    AstroAppTheme(themePreference = 0) {
        MainTopAppBar(
            title = "Mars Photos",
            selectedDate = LocalDate.of(2024, 1, 15),
            maxDate = LocalDate.now(),
            onDateSelected = {}
        )
    }
}

@Preview(
    name = "Top App Bar with date picker - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MainTopAppBarWithDateDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        MainTopAppBar(
            title = "Mars Photos",
            selectedDate = LocalDate.of(2024, 1, 15),
            maxDate = LocalDate.now(),
            onDateSelected = {}
        )
    }
}
