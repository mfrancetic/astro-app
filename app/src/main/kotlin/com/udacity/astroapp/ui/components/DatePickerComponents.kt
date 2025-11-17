package com.udacity.astroapp.ui.components

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePickerButton(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.select_date),
    dateFormat: String = "yyyy-MM-dd",
    minDate: Long? = null,
    maxDate: Long? = null
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }

    // Parse selected date or use current date
    val currentDateInMillis =
        remember(selectedDate) {
            if (selectedDate != null) {
                try {
                    dateFormatter.parse(selectedDate)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } else {
                System.currentTimeMillis()
            }
        }

    Button(
        onClick = {
            calendar.timeInMillis = currentDateInMillis

            val datePickerDialog =
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val formattedDate = dateFormatter.format(calendar.time)
                        onDateSelected(formattedDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

            // Set min/max dates if provided
            minDate?.let { datePickerDialog.datePicker.minDate = it }
            maxDate?.let { datePickerDialog.datePicker.maxDate = it }

            datePickerDialog.show()
        },
        modifier = modifier
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.date_picker_icon_size))
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.date_picker_icon_spacing)))
        Text(selectedDate ?: label)
    }
}

@Composable
fun DateRangePicker(
    startDate: String?,
    endDate: String?,
    onStartDateSelected: (String) -> Unit,
    onEndDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: String = "yyyy-MM-dd",
    startLabel: String = "Start Date",
    endLabel: String = "End Date"
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            DatePickerButton(
                selectedDate = startDate,
                onDateSelected = onStartDateSelected,
                modifier = Modifier.weight(1f),
                label = startLabel,
                dateFormat = dateFormat,
                maxDate =
                    if (endDate != null) {
                        try {
                            SimpleDateFormat(dateFormat, Locale.getDefault()).parse(endDate)?.time
                        } catch (e: Exception) {
                            null
                        }
                    } else null
            )

            DatePickerButton(
                selectedDate = endDate,
                onDateSelected = onEndDateSelected,
                modifier = Modifier.weight(1f),
                label = endLabel,
                dateFormat = dateFormat,
                minDate =
                    if (startDate != null) {
                        try {
                            SimpleDateFormat(dateFormat, Locale.getDefault()).parse(startDate)?.time
                        } catch (e: Exception) {
                            null
                        }
                    } else null
            )
        }
    }
}

@Composable
fun DateFilterCard(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    onClearDate: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "Filter by Date",
    dateFormat: String = "yyyy-MM-dd"
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerButton(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.weight(1f),
                    dateFormat = dateFormat
                )

                if (selectedDate != null && onClearDate != null) {
                    TextButton(onClick = onClearDate) { Text(stringResource(R.string.clear)) }
                }
            }
        }
    }
}

@Composable
fun DateRangeFilterCard(
    startDate: String?,
    endDate: String?,
    onStartDateSelected: (String) -> Unit,
    onEndDateSelected: (String) -> Unit,
    onClearRange: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "Filter by Date Range",
    dateFormat: String = "yyyy-MM-dd"
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)

                if ((startDate != null || endDate != null) && onClearRange != null) {
                    TextButton(onClick = onClearRange) { Text(stringResource(R.string.clear)) }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            DateRangePicker(
                startDate = startDate,
                endDate = endDate,
                onStartDateSelected = onStartDateSelected,
                onEndDateSelected = onEndDateSelected,
                dateFormat = dateFormat
            )
        }
    }
}

@Composable
fun QuickDateSelector(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: String = "yyyy-MM-dd"
) {
    val dateFormatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        // Today
        Button(
            onClick = {
                calendar.timeInMillis = System.currentTimeMillis()
                onDateSelected(dateFormatter.format(calendar.time))
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.today))
        }

        // Yesterday
        Button(
            onClick = {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                onDateSelected(dateFormatter.format(calendar.time))
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.yesterday))
        }

        // Last Week
        Button(
            onClick = {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_MONTH, -7)
                onDateSelected(dateFormatter.format(calendar.time))
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.last_week))
        }
    }
}

@Composable
fun CompactDatePicker(
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateFormat: String = "MMM dd, yyyy",
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    OutlinedButton(
        onClick = {
            val currentDateInMillis =
                if (selectedDate != null) {
                    try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .parse(selectedDate)
                            ?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                } else {
                    System.currentTimeMillis()
                }

            calendar.timeInMillis = currentDateInMillis

            DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val formattedDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(calendar.time)
                        onDateSelected(formattedDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                .show()
        },
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.card_padding))
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
        Text(
            if (selectedDate != null) {
                try {
                    val date =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                    displayFormatter.format(date!!)
                } catch (e: Exception) {
                    selectedDate
                }
            } else {
                stringResource(R.string.select_date)
            }
        )
    }
}

// Date Picker Components Previews
@Preview(name = "Date Picker Button - Light", showBackground = true)
@Composable
private fun DatePickerButtonLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DatePickerButton(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(
    name = "Date Picker Button - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DatePickerButtonDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DatePickerButton(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(name = "Date Picker Button Empty - Light", showBackground = true)
@Composable
private fun DatePickerButtonEmptyLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DatePickerButton(selectedDate = null, onDateSelected = {}, label = "Select Date")
    }
}

@Preview(
    name = "Date Picker Button Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DatePickerButtonEmptyDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DatePickerButton(selectedDate = null, onDateSelected = {}, label = "Select Date")
    }
}

@Preview(name = "Date Range Picker - Light", showBackground = true)
@Composable
private fun DateRangePickerLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DateRangePicker(
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            onStartDateSelected = {},
            onEndDateSelected = {}
        )
    }
}

@Preview(
    name = "Date Range Picker - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DateRangePickerDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DateRangePicker(
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            onStartDateSelected = {},
            onEndDateSelected = {}
        )
    }
}

@Preview(name = "Date Filter Card - Light", showBackground = true)
@Composable
private fun DateFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DateFilterCard(
            selectedDate = "2024-01-15",
            onDateSelected = {},
            onClearDate = {},
            title = "Filter by Date"
        )
    }
}

@Preview(
    name = "Date Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DateFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DateFilterCard(
            selectedDate = "2024-01-15",
            onDateSelected = {},
            onClearDate = {},
            title = "Filter by Date"
        )
    }
}

@Preview(name = "Date Filter Card Empty - Light", showBackground = true)
@Composable
private fun DateFilterCardEmptyLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DateFilterCard(selectedDate = null, onDateSelected = {}, title = "Filter by Date")
    }
}

@Preview(
    name = "Date Filter Card Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DateFilterCardEmptyDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DateFilterCard(selectedDate = null, onDateSelected = {}, title = "Filter by Date")
    }
}

@Preview(name = "Date Range Filter Card - Light", showBackground = true)
@Composable
private fun DateRangeFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        DateRangeFilterCard(
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            onStartDateSelected = {},
            onEndDateSelected = {},
            onClearRange = {},
            title = "Filter by Date Range"
        )
    }
}

@Preview(
    name = "Date Range Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DateRangeFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        DateRangeFilterCard(
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            onStartDateSelected = {},
            onEndDateSelected = {},
            onClearRange = {},
            title = "Filter by Date Range"
        )
    }
}

@Preview(name = "Quick Date Selector - Light", showBackground = true)
@Composable
private fun QuickDateSelectorLightPreview() {
    AstroAppTheme(themePreference = 0) {
        QuickDateSelector(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(
    name = "Quick Date Selector - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun QuickDateSelectorDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        QuickDateSelector(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(name = "Compact Date Picker - Light", showBackground = true)
@Composable
private fun CompactDatePickerLightPreview() {
    AstroAppTheme(themePreference = 0) {
        CompactDatePicker(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(
    name = "Compact Date Picker - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CompactDatePickerDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        CompactDatePicker(selectedDate = "2024-01-15", onDateSelected = {})
    }
}

@Preview(name = "Compact Date Picker Empty - Light", showBackground = true)
@Composable
private fun CompactDatePickerEmptyLightPreview() {
    AstroAppTheme(themePreference = 0) {
        CompactDatePicker(selectedDate = null, onDateSelected = {})
    }
}

@Preview(
    name = "Compact Date Picker Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CompactDatePickerEmptyDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        CompactDatePicker(selectedDate = null, onDateSelected = {})
    }
}
