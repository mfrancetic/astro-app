package com.udacity.astroapp.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.theme.AstroAppTheme

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
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.search_content_description)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_content_description)
                    )
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
    Column(modifier = modifier) {
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
                        val newSelection =
                            if (isSelected) {
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
    Column(modifier = modifier) {
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
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
                Text(text = title, style = MaterialTheme.typography.titleMedium)
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
    val rovers =
        listOf(
            stringResource(R.string.rover_curiosity),
            stringResource(R.string.rover_opportunity),
            stringResource(R.string.rover_spirit),
            stringResource(R.string.rover_perseverance),
            stringResource(R.string.rover_ingenuity)
        )
    val cameras =
        listOf(
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

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
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
    CardComponent(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = hazardousOnly, onCheckedChange = onHazardousOnlyChange)
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(stringResource(R.string.filter_hazardous_only))
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

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            Text(text = "Observatory Filters", style = MaterialTheme.typography.titleMedium)

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = openNowOnly, onCheckedChange = onOpenNowOnlyChange)
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
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.spacing_medium)),
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
                    TextButton(onClick = onClearAll) {
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

// Search and Filter Components Previews
@Preview(name = "Search Bar Empty - Light", showBackground = true)
@Composable
private fun SearchBarEmptyLightPreview() {
    AstroAppTheme(themePreference = 0) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onSearch = {},
            placeholder = "Search observatories..."
        )
    }
}

@Preview(
    name = "Search Bar Empty - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchBarEmptyDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onSearch = {},
            placeholder = "Search observatories..."
        )
    }
}

@Preview(name = "Search Bar Filled - Light", showBackground = true)
@Composable
private fun SearchBarFilledLightPreview() {
    AstroAppTheme(themePreference = 0) {
        SearchBar(
            query = "Griffith",
            onQueryChange = {},
            onSearch = {},
            placeholder = "Search observatories..."
        )
    }
}

@Preview(
    name = "Search Bar Filled - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchBarFilledDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        SearchBar(
            query = "Griffith",
            onQueryChange = {},
            onSearch = {},
            placeholder = "Search observatories..."
        )
    }
}

@Preview(name = "Filter Chip Selected - Light", showBackground = true)
@Composable
private fun FilterChipSelectedLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FilterChip(label = "Hazardous", selected = true, onSelectedChange = {})
    }
}

@Preview(
    name = "Filter Chip Selected - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FilterChipSelectedDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FilterChip(label = "Hazardous", selected = true, onSelectedChange = {})
    }
}

@Preview(name = "Filter Chip Unselected - Light", showBackground = true)
@Composable
private fun FilterChipUnselectedLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FilterChip(label = "Safe", selected = false, onSelectedChange = {})
    }
}

@Preview(
    name = "Filter Chip Unselected - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FilterChipUnselectedDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FilterChip(label = "Safe", selected = false, onSelectedChange = {})
    }
}

@Preview(name = "Filter Chip Group - Light", showBackground = true)
@Composable
private fun FilterChipGroupLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FilterChipGroup(
            title = "Cameras",
            options = listOf("NAVCAM", "MAST", "FHAZ", "RHAZ"),
            selectedOptions = setOf("NAVCAM", "MAST"),
            onSelectionChange = {}
        )
    }
}

@Preview(
    name = "Filter Chip Group - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FilterChipGroupDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FilterChipGroup(
            title = "Cameras",
            options = listOf("NAVCAM", "MAST", "FHAZ", "RHAZ"),
            selectedOptions = setOf("NAVCAM", "MAST"),
            onSelectionChange = {}
        )
    }
}

@Preview(name = "Single Select Filter Group - Light", showBackground = true)
@Composable
private fun SingleSelectFilterGroupLightPreview() {
    AstroAppTheme(themePreference = 0) {
        SingleSelectFilterGroup(
            title = "Rover",
            options = listOf("Curiosity", "Perseverance", "Opportunity", "Spirit"),
            selectedOption = "Curiosity",
            onSelectionChange = {}
        )
    }
}

@Preview(
    name = "Single Select Filter Group - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SingleSelectFilterGroupDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        SingleSelectFilterGroup(
            title = "Rover",
            options = listOf("Curiosity", "Perseverance", "Opportunity", "Spirit"),
            selectedOption = "Curiosity",
            onSelectionChange = {}
        )
    }
}

@Preview(name = "Search Filter Card - Light", showBackground = true)
@Composable
private fun SearchFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        SearchFilterCard(
            searchQuery = "",
            onSearchQueryChange = {},
            onSearch = {},
            title = "Search & Filter",
            searchPlaceholder = "Search asteroids..."
        )
    }
}

@Preview(
    name = "Search Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        SearchFilterCard(
            searchQuery = "Apollo",
            onSearchQueryChange = {},
            onSearch = {},
            title = "Search & Filter",
            searchPlaceholder = "Search asteroids..."
        )
    }
}

@Preview(name = "Rover Filter Card - Light", showBackground = true)
@Composable
private fun RoverFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        RoverFilterCard(
            selectedRover = "Curiosity",
            onRoverSelected = {},
            selectedCamera = "NAVCAM",
            onCameraSelected = {}
        )
    }
}

@Preview(
    name = "Rover Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RoverFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        RoverFilterCard(
            selectedRover = "Perseverance",
            onRoverSelected = {},
            selectedCamera = "MAST",
            onCameraSelected = {}
        )
    }
}

@Preview(name = "Asteroid Filter Card - Light", showBackground = true)
@Composable
private fun AsteroidFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        AsteroidFilterCard(hazardousOnly = true, onHazardousOnlyChange = {})
    }
}

@Preview(
    name = "Asteroid Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AsteroidFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        AsteroidFilterCard(hazardousOnly = false, onHazardousOnlyChange = {})
    }
}

@Preview(name = "Observatory Filter Card - Light", showBackground = true)
@Composable
private fun ObservatoryFilterCardLightPreview() {
    AstroAppTheme(themePreference = 0) {
        ObservatoryFilterCard(
            searchRadius = 25,
            onSearchRadiusChange = {},
            openNowOnly = true,
            onOpenNowOnlyChange = {}
        )
    }
}

@Preview(
    name = "Observatory Filter Card - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ObservatoryFilterCardDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        ObservatoryFilterCard(
            searchRadius = 50,
            onSearchRadiusChange = {},
            openNowOnly = false,
            onOpenNowOnlyChange = {}
        )
    }
}

@Preview(name = "Filter Summary - Light", showBackground = true)
@Composable
private fun FilterSummaryLightPreview() {
    AstroAppTheme(themePreference = 0) {
        FilterSummary(
            activeFilters = listOf("Hazardous Only", "Date: 2024-01-15", "Rover: Curiosity"),
            onClearAll = {}
        )
    }
}

@Preview(
    name = "Filter Summary - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FilterSummaryDarkPreview() {
    AstroAppTheme(themePreference = 1) {
        FilterSummary(activeFilters = listOf("Open Now", "Radius: 25km"), onClearAll = {})
    }
}
