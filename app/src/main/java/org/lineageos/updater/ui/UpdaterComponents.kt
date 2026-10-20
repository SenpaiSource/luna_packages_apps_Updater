/*
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lineageos.updater.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun UpdaterEmblem(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier.clip(MaterialShapes.Cookie9Sided.toShape()).background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.fillMaxSize(0.45f), tint = contentColor)
    }
}

@Composable
fun UpdaterSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier.padding(horizontal = 4.dp).semantics { heading() },
        style = MaterialTheme.typography.titleLargeEmphasized,
    )
}

@Composable
fun UpdaterGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        UpdaterSectionTitle(title)
        Column(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            content = content,
        )
    }
}

@Composable
fun UpdaterActionSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            if (pressed) 0.97f else 1f,
            MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "card press",
        )
    val corner by
        animateDpAsState(
            if (pressed) 20.dp else 28.dp,
            MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "card corner",
        )
    Surface(
        onClick = onClick,
        modifier =
            modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = shape ?: RoundedCornerShape(corner),
        color = color,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun UpdaterActionRow(
    title: String,
    summary: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    UpdaterActionSurface(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(4.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .heightIn(min = 88.dp)
                .graphicsLayer { alpha = if (enabled) 1f else 0.38f }
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UpdaterEmblem(icon, Modifier.size(44.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
        }
    }
}

@Composable
fun UpdaterSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    highlighted: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor by
        animateColorAsState(
            when {
                highlighted && checked -> colors.primaryContainer
                highlighted -> colors.surfaceContainerHigh
                else -> colors.surfaceContainerLow
            },
            MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "switch card color",
        )
    val contentColor = if (highlighted && checked) colors.onPrimaryContainer else colors.onSurface
    Surface(color = containerColor, contentColor = contentColor, shape = RoundedCornerShape(4.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .toggleable(
                    value = checked,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f).graphicsLayer { alpha = if (enabled) 1f else 0.38f },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (highlighted && checked) colors.onPrimaryContainer
                        else colors.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                interactionSource = interactionSource,
                thumbContent = {
                    Crossfade(
                        targetState = checked,
                        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        label = "switch thumb",
                    ) { isChecked ->
                        Icon(
                            if (isChecked) Icons.Rounded.Check else Icons.Rounded.Close,
                            null,
                            Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimary,
                        checkedTrackColor = colors.primary,
                        checkedIconColor = colors.primary,
                        uncheckedThumbColor = colors.outline,
                        uncheckedTrackColor = colors.surfaceContainerHighest,
                        uncheckedBorderColor = Color.Transparent,
                        uncheckedIconColor = colors.surfaceContainerHighest,
                        disabledUncheckedBorderColor = Color.Transparent,
                    ),
            )
        }
    }
}
