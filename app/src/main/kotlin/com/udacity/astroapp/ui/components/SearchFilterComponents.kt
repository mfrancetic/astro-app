package com.udacity.astroapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.udacity.astroapp.R

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_placeholder),
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_content_description))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_content_description))
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        selected = selected,
        modifier = modifier
    )
}

@Composable
fun FilterChipGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Column(
        modifier = modifier
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.selectableGroup()
        ) {
            options.forEach { option ->
                FilterChip(
                    label = option,
                    selected = option in selectedOptions,
                    onSelectedChange = { isSelected ->
                        val newSelection = if (isSelected) {
                            selectedOptions + option
                        } else {
                            selectedOptions - option
                        }
                        onSelectionChange(newSelection)
                    }
                )
            }
        }
    }
}

@Composable
fun SingleSelectFilterGroup(
    options: List<String>,
    selectedOption: String?,
    onSelectionChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    allowDeselection: Boolean = true
) {
    Column(
        modifier = modifier
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small)),
            modifier = Modifier.selectableGroup()
        ) {
            options.forEach { option ->
                FilterChip(
                    label = option,
                    selected = option == selectedOption,
                    onSelectedChange = { isSelected ->
                        if (isSelected) {
                            onSelectionChange(option)
                        } else if (allowDeselection) {
                            onSelectionChange(null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchFilterCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Search & Filter",
    searchPlaceholder: String = "Search...",
    additionalFilters: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_small))
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                placeholder = searchPlaceholder
            )

            additionalFilters?.let {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))
                it()
            }
        }
    }
}

@Composable
fun RoverFilterCard(
    selectedRover: String?,
    onRoverSelected: (String?) -> Unit,
    selectedCamera: String?,
    onCameraSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val rovers = listOf(
        stringResource(R.string.rover_curiosity),
        stringResource(R.string.rover_opportunity),
        stringResource(R.string.rover_spirit),
        stringResource(R.string.rover_perseverance),
        stringResource(R.string.rover_ingenuity)
    )
    val cameras = listOf(
        stringResource(R.string.camera_fhaz),
        stringResource(R.string.camera_rhaz),
        stringResource(R.string.camera_mast),
        stringResource(R.string.camera_chemcam),
        stringResource(R.string.camera_mahli),
        stringResource(R.string.camera_mardi),
        stringResource(R.string.camera_navcam),
        stringResource(R.string.camera_pancam),
        stringResource(R.string.camera_minites)
    )

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Text(
                text = stringResource(R.string.mars_rover_filters),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            SingleSelectFilterGroup(
                title = stringResource(R.string.filter_rover),
                options = rovers,
                selectedOption = selectedRover,
                onSelectionChange = onRoverSelected
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            SingleSelectFilterGroup(
                title = stringResource(R.string.filter_camera),
                options = cameras,
                selectedOption = selectedCamera,
                onSelectionChange = onCameraSelected
            )
        }
    }
}

@Composable
fun AsteroidFilterCard(
    hazardousOnly: Boolean,
    onHazardousOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Text(
                text = stringResource(R.string.asteroid_filters),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hazardousOnly,
                    onCheckedChange = onHazardousOnlyChange
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(stringResource(R.string.filter_hazardous_only))
            }
        }
    }
}

@Composable
fun ObservatoryFilterCard(
    searchRadius: Int,
    onSearchRadiusChange: (Int) -> Unit,
    openNowOnly: Boolean,
    onOpenNowOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val radiusOptions = listOf(5, 10, 25, 50, 100)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))
        ) {
            Text(
                text = "Observatory Filters",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            SingleSelectFilterGroup(
                title = "Search Radius (km)",
                options = radiusOptions.map { "${it}km" },
                selectedOption = "${searchRadius}km",
                onSelectionChange = { selected ->
                    selected?.let { selection ->
                        val radius = selection.removeSuffix("km").toIntOrNull()
                        if (radius != null) {
                            onSearchRadiusChange(radius)
                        }
                    }
                },
                allowDeselection = false
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.card_padding)))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = openNowOnly,
                    onCheckedChange = onOpenNowOnlyChange
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(stringResource(R.string.show_only_open_observatories))
            }
        }
    }
}

@Composable
fun FilterSummary(
    activeFilters: List<String>,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (activeFilters.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_medium)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters: ${activeFilters.joinString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                if (onClearAll != null) {
                    TextButton(
                        onClick = onClearAll
                    ) {
                        Text(
                            text = "Clear All",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// Extension function for joining strings
private fun List<String>.joinString(separator: String): String {
    return this.joinToString(separator)
}