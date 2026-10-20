/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package org.lineageos.updater.updates

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import org.lineageos.updater.R
import org.lineageos.updater.ui.UpdaterEmblem
import org.lineageos.updater.ui.UpdaterTheme
import org.lineageos.updater.updates.action.UpdateAction
import org.lineageos.updater.updates.action.UpdateActionType
import org.lineageos.updater.updates.action.UpdateActions
import org.lineageos.updater.updates.button.ActionBar
import org.lineageos.updater.updates.button.ActionBarButton
import org.lineageos.updater.updates.button.ActionBarMenuItem
import org.lineageos.updater.updates.state.ProgressState
import org.lineageos.updater.updates.state.UpdateItemState

@Composable
fun UpdateItem(
    state: UpdateItemState,
    expanded: Boolean,
    onExpandToggle: (() -> Unit)?,
    onAction: (UpdateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val containerColor by
        animateColorAsState(
            if (state.progress != null) colors.surfaceContainerHigh else colors.surfaceContainerLow,
            MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "update surface",
        )
    val arrowRotation by
        animateFloatAsState(
            if (expanded) 180f else 0f,
            MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "update expansion",
        )
    val expansionLabel =
        stringResource(
            if (expanded) R.string.updater_collapse_update else R.string.updater_expand_update
        )
    val expansionState =
        stringResource(if (expanded) R.string.updater_expanded else R.string.updater_collapsed)
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = containerColor) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .then(
                        if (onExpandToggle != null) {
                            Modifier.clickable(
                                    role = Role.Button,
                                    onClickLabel = expansionLabel,
                                    onClick = onExpandToggle,
                                )
                                .semantics { stateDescription = expansionState }
                        } else Modifier
                    )
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UpdaterEmblem(
                    if (state.isLocal) Icons.Rounded.Archive else Icons.Rounded.CloudDownload,
                    Modifier.size(48.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(state.buildDate, style = MaterialTheme.typography.titleLargeEmphasized)
                    Text(
                        state.buildVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    if (state.status.isNotEmpty()) {
                        Text(
                            state.status,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                }
                if (onExpandToggle != null) {
                    Icon(Icons.Rounded.ExpandMore, null, Modifier.rotate(arrowRotation))
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter =
                    fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
                        expandVertically(
                            MaterialTheme.motionScheme.defaultSpatialSpec(),
                            expandFrom = Alignment.Top,
                        ),
                exit =
                    fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
                        shrinkVertically(
                            MaterialTheme.motionScheme.fastSpatialSpec(),
                            shrinkTowards = Alignment.Top,
                        ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        when (val progress = state.progress) {
                            is ProgressState.Determinate -> {
                                val fraction by
                                    animateFloatAsState(
                                        (progress.percent / 100f).coerceIn(0f, 1f),
                                        MaterialTheme.motionScheme.fastEffectsSpec(),
                                        label = "update progress",
                                    )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        "${progress.percent.toInt()}%",
                                        style = MaterialTheme.typography.displaySmallEmphasized,
                                        color = colors.primary,
                                    )
                                    Column(Modifier.align(Alignment.CenterVertically)) {
                                        if (progress.downloadedSize.isNotEmpty()) {
                                            Text(
                                                progress.downloadedSize,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                        if (progress.eta.isNotEmpty()) {
                                            Text(
                                                progress.eta,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                                LinearWavyProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier.fillMaxWidth(),
                                    waveSpeed =
                                        if (
                                            state.actions.primary.type ==
                                                UpdateActionType.RESUME_DOWNLOAD ||
                                                state.actions.primary.type ==
                                                    UpdateActionType.RESUME_INSTALL
                                        ) {
                                            0.dp
                                        } else
                                            WavyProgressIndicatorDefaults
                                                .LinearDeterminateWavelength,
                                )
                            }
                            ProgressState.Indeterminate -> {
                                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            null -> {
                                UpdateDetail(
                                    Icons.Rounded.Storage,
                                    stringResource(R.string.list_update_size, state.fileSize),
                                )
                                if (state.androidUpdateInfo.isNotEmpty()) {
                                    UpdateDetail(Icons.Rounded.Android, state.androidUpdateInfo)
                                }
                                if (state.securityUpdate.isNotEmpty()) {
                                    UpdateDetail(
                                        Icons.Rounded.Security,
                                        stringResource(
                                            R.string.list_security_update,
                                            state.securityUpdate,
                                        ),
                                    )
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = colors.secondaryContainer,
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(Icons.Rounded.Info, null, Modifier.size(20.dp))
                                        Text(
                                            if (
                                                state.installNote ==
                                                    R.string.list_major_upgrade_recovery_install
                                            ) {
                                                stringResource(
                                                    state.installNote,
                                                    stringResource(R.string.brand_name),
                                                )
                                            } else stringResource(state.installNote),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ActionBar(
                        buttons =
                            listOfNotNull(
                                state.actions.primary.toActionBarButton(
                                    context,
                                    onAction,
                                    isPrimary = true,
                                ),
                                state.actions.secondary?.toActionBarButton(context, onAction),
                            ),
                        menuItems =
                            state.actions.overflow.map { action ->
                                ActionBarMenuItem(action.type.title(context), action.enabled) {
                                    onAction(action)
                                }
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateDetail(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun UpdateAction.toActionBarButton(
    context: Context,
    onAction: (UpdateAction) -> Unit,
    isPrimary: Boolean = false,
): ActionBarButton =
    when (type) {
        UpdateActionType.PAUSE_DOWNLOAD,
        UpdateActionType.PAUSE_INSTALL ->
            ActionBarButton.Icon(
                imageVector = Icons.Outlined.Pause,
                contentDescription = type.title(context),
                enabled = enabled,
                onClick = { onAction(this) },
            )

        UpdateActionType.RESUME_DOWNLOAD,
        UpdateActionType.RESUME_INSTALL ->
            ActionBarButton.Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = type.title(context),
                enabled = enabled,
                onClick = { onAction(this) },
            )

        else ->
            if (isPrimary) {
                ActionBarButton.Primary(
                    text = type.title(context),
                    enabled = enabled,
                    onClick = { onAction(this) },
                )
            } else {
                ActionBarButton.Outlined(
                    text = type.title(context),
                    enabled = enabled,
                    onClick = { onAction(this) },
                )
            }
    }

@UiModePreviews
@Composable
private fun UpdateItemIdleCollapsedPreview() {
    UpdaterTheme {
        UpdateItem(
            state =
                UpdateItemState(
                    downloadId = "preview",
                    buildDate = "Apr 27",
                    buildVersion = "Lunaris AOSP 3.12",
                    status = "",
                    isLocal = false,
                    fileSize = "1.1 GB",
                    androidUpdateInfo = "Android 16",
                    securityUpdate = "Jan 2026",
                    installNote = R.string.list_full_install,
                    progress = null,
                    actions =
                        UpdateActions(
                            primary = UpdateAction(type = UpdateActionType.START_DOWNLOAD),
                            overflow = listOf(UpdateAction(type = UpdateActionType.VIEW_DOWNLOADS)),
                        ),
                ),
            expanded = false,
            onExpandToggle = {},
            onAction = {},
        )
    }
}

@UiModePreviews
@Composable
private fun UpdateItemIdleExpandedPreview() {
    UpdaterTheme {
        UpdateItem(
            state =
                UpdateItemState(
                    downloadId = "preview",
                    buildDate = "Apr 27",
                    buildVersion = "Lunaris AOSP 3.12",
                    status = "",
                    isLocal = false,
                    fileSize = "1.1 GB",
                    androidUpdateInfo = "Major Android upgrade",
                    securityUpdate = "Jan 2026",
                    installNote = R.string.list_major_upgrade_recovery_install,
                    progress = null,
                    actions =
                        UpdateActions(primary = UpdateAction(type = UpdateActionType.OPEN_GUIDE)),
                ),
            expanded = true,
            onExpandToggle = {},
            onAction = {},
        )
    }
}

@UiModePreviews
@Composable
private fun UpdateItemDownloadingPreview() {
    UpdaterTheme {
        UpdateItem(
            state =
                UpdateItemState(
                    downloadId = "preview",
                    buildDate = "Apr 27",
                    buildVersion = "Lunaris AOSP 3.12",
                    status = "Downloading",
                    isLocal = false,
                    fileSize = "1.1 GB",
                    androidUpdateInfo = "Android 16",
                    securityUpdate = "Jan 2026",
                    installNote = R.string.list_full_install,
                    progress =
                        ProgressState.Determinate(
                            percent = 65f,
                            downloadedSize = "715 MB of 1.1 GB",
                            eta = "3 min left",
                        ),
                    actions =
                        UpdateActions(
                            primary = UpdateAction(type = UpdateActionType.PAUSE_DOWNLOAD),
                            secondary = UpdateAction(type = UpdateActionType.CANCEL_DOWNLOAD),
                            overflow = listOf(UpdateAction(type = UpdateActionType.VIEW_DOWNLOADS)),
                        ),
                ),
            expanded = true,
            onExpandToggle = {},
            onAction = {},
        )
    }
}
