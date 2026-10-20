/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package org.lineageos.updater.updates.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.updater.R

@Composable
fun ActionBar(buttons: List<ActionBarButton>, menuItems: List<ActionBarMenuItem> = emptyList()) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        buttons.forEach { button ->
            when (button) {
                is ActionBarButton.Primary ->
                    Button(
                        onClick = button.onClick,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shapes = ButtonDefaults.shapesFor(48.dp),
                        contentPadding = ButtonDefaults.contentPaddingFor(48.dp),
                        enabled = button.enabled,
                    ) {
                        Text(button.text)
                    }
                is ActionBarButton.Outlined ->
                    OutlinedButton(
                        onClick = button.onClick,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shapes = ButtonDefaults.shapesFor(48.dp),
                        contentPadding = ButtonDefaults.contentPaddingFor(48.dp),
                        enabled = button.enabled,
                    ) {
                        Text(button.text)
                    }
                is ActionBarButton.Icon ->
                    FilledIconButton(
                        onClick = button.onClick,
                        modifier = Modifier.size(48.dp),
                        shapes = IconButtonDefaults.shapes(),
                        enabled = button.enabled,
                    ) {
                        Icon(button.imageVector, button.contentDescription)
                    }
            }
        }
        if (menuItems.isNotEmpty()) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box(Modifier.align(Alignment.CenterVertically)) {
                FilledTonalIconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(48.dp),
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(Icons.Rounded.MoreHoriz, stringResource(R.string.updater_more_actions))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    menuItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.text) },
                            enabled = item.enabled,
                            onClick = {
                                menuExpanded = false
                                item.onClick()
                            },
                        )
                    }
                }
            }
        }
    }
}

data class ActionBarMenuItem(
    val text: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

sealed interface ActionBarButton {
    val enabled: Boolean
    val onClick: () -> Unit

    data class Primary(
        val text: String,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionBarButton

    data class Outlined(
        val text: String,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionBarButton

    data class Icon(
        val imageVector: ImageVector,
        val contentDescription: String? = null,
        override val enabled: Boolean = true,
        override val onClick: () -> Unit,
    ) : ActionBarButton
}
