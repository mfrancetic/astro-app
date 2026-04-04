package com.udacity.astroapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.udacity.astroapp.R

@Composable
fun CardComponent(
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit),
) {
    Box(modifier) {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.card_padding))) {
                content()
            }
        }
    }
}
