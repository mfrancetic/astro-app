package com.udacity.astroapp.ui.components

import android.app.DatePickerDialog
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
import com.udacity.astroapp.R
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
