/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.updater.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollapseBar(
    expanded: Boolean,
    hiddenItemCount: Int,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = { onExpandedChange(!expanded) },
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
        Spacer(Modifier.width(8.dp))
        Text(
            if (expanded) stringResource(R.string.see_less)
            else stringResource(R.string.see_more, hiddenItemCount)
        )
    }
}
