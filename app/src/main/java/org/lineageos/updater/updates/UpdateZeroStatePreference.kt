/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lineageos.updater.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import org.lineageos.updater.ui.UpdaterEmblem
import org.lineageos.updater.ui.UpdaterTheme

@Composable
fun UpdateZeroStatePreference(
    modifier: Modifier = Modifier,
    text: String = "",
    description: String = "",
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer,
    ) {
        Column(
            Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UpdaterEmblem(
                Icons.Rounded.DoneAll,
                Modifier.size(72.dp),
                containerColor = colors.tertiary,
                contentColor = colors.onTertiary,
            )
            if (text.isNotEmpty()) {
                Text(
                    text,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    textAlign = TextAlign.Center,
                )
            }
            if (description.isNotEmpty()) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@UiModePreviews
@Composable
private fun UpdateZeroStatePreferencePreview() {
    UpdaterTheme {
        UpdateZeroStatePreference(
            text = "No updates available",
            description = "New builds for this device will appear here when available.",
        )
    }
}
