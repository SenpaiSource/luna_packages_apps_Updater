/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package org.lineageos.updater.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import kotlinx.coroutines.launch
import org.lineageos.updater.R
import org.lineageos.updater.UpdaterApplication
import org.lineageos.updater.data.CheckInterval
import org.lineageos.updater.data.UserPreferencesRepository
import org.lineageos.updater.deviceinfo.DeviceInfoUtils
import org.lineageos.updater.ui.UpdaterEmblem
import org.lineageos.updater.ui.UpdaterGroup
import org.lineageos.updater.ui.UpdaterScaffold
import org.lineageos.updater.ui.UpdaterSwitchRow
import org.lineageos.updater.util.BatteryMonitor

@Composable
fun PreferencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as UpdaterApplication }
    val isABDevice = remember { DeviceInfoUtils.isABDevice }
    val showRecoveryUpdate = remember { installRecoveryScriptExists() }
    UpdaterScaffold(title = stringResource(R.string.updater_preferences_title), onBack = onBack) {
        padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UpdaterEmblem(
                            Icons.Rounded.Tune,
                            Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        )
                        Text(
                            stringResource(R.string.updater_preferences_description),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                PreferencesContent(
                    application.userPreferencesRepository,
                    application.batteryMonitor,
                    isABDevice,
                    showRecoveryUpdate,
                )
            }
        }
    }
}

@Composable
private fun PreferencesContent(
    repository: UserPreferencesRepository,
    batteryMonitor: BatteryMonitor,
    isABDevice: Boolean,
    showRecoveryUpdate: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val abPerfMode by repository.abPerfModeFlow.collectAsStateWithLifecycle(false)
    val batteryState by
        batteryMonitor.batteryState.collectAsStateWithLifecycle(batteryMonitor.currentBatteryState)
    val autoDelete by repository.autoDeleteFlow.collectAsStateWithLifecycle(true)
    val streamUpdates by repository.streamUpdatesFlow.collectAsStateWithLifecycle(true)
    val checkInterval by
        repository.checkIntervalFlow.collectAsStateWithLifecycle(CheckInterval.default)
    val meteredNetworkWarning by
        repository.meteredNetworkWarningFlow.collectAsStateWithLifecycle(true)
    val periodicCheckEnabled by
        repository.periodicCheckEnabledFlow.collectAsStateWithLifecycle(true)
    var recoveryUpdateEnabled by remember { mutableStateOf(repository.getRecoveryUpdateEnabled()) }

    UpdaterGroup(title = stringResource(R.string.pref_category_background_sync)) {
        UpdaterSwitchRow(
            title = stringResource(R.string.menu_auto_updates_check),
            summary = stringResource(R.string.menu_auto_updates_check_summary),
            checked = periodicCheckEnabled,
            highlighted = true,
            onCheckedChange = { value ->
                coroutineScope.launch { repository.setPeriodicCheckEnabled(value) }
            },
        )
        CheckIntervalSelector(
            selectedInterval = checkInterval,
            enabled = periodicCheckEnabled,
            onSelect = { interval ->
                coroutineScope.launch { repository.setCheckInterval(interval) }
            },
        )
    }

    UpdaterGroup(title = stringResource(R.string.pref_category_download_install)) {
        if (isABDevice) {
            UpdaterSwitchRow(
                title = stringResource(R.string.menu_stream_updates),
                summary = stringResource(R.string.menu_stream_updates_summary),
                checked = streamUpdates,
                onCheckedChange = { value ->
                    coroutineScope.launch { repository.setStreamUpdates(value) }
                },
            )
        } else {
            UpdaterSwitchRow(
                title = stringResource(R.string.menu_auto_delete_updates),
                summary = stringResource(R.string.menu_auto_delete_updates_summary),
                checked = autoDelete,
                onCheckedChange = { value ->
                    coroutineScope.launch { repository.setAutoDelete(value) }
                },
            )
        }
        UpdaterSwitchRow(
            title = stringResource(R.string.menu_metered_network_warning),
            summary = stringResource(R.string.menu_metered_network_warning_summary),
            checked = meteredNetworkWarning,
            onCheckedChange = { value ->
                coroutineScope.launch { repository.setMeteredNetworkWarning(value) }
            },
        )
        if (isABDevice) {
            UpdaterSwitchRow(
                title = stringResource(R.string.menu_ab_perf_mode),
                summary =
                    stringResource(
                        if (batteryState.isAcCharging) R.string.menu_ab_perf_mode_summary_charging
                        else R.string.menu_ab_perf_mode_summary
                    ),
                checked = batteryState.isAcCharging || abPerfMode,
                enabled = !batteryState.isAcCharging,
                onCheckedChange = { value ->
                    coroutineScope.launch { repository.setAbPerfMode(value) }
                },
            )
        }
        if (showRecoveryUpdate) {
            UpdaterSwitchRow(
                title = stringResource(R.string.menu_update_recovery),
                summary = stringResource(R.string.menu_update_recovery_summary),
                checked = recoveryUpdateEnabled,
                onCheckedChange = { value ->
                    recoveryUpdateEnabled = value
                    repository.setRecoveryUpdateEnabled(value)
                },
            )
        }
    }
}

@Composable
private fun CheckIntervalSelector(
    selectedInterval: CheckInterval,
    enabled: Boolean,
    onSelect: (CheckInterval) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    null,
                    Modifier.size(20.dp),
                    tint =
                        MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f),
                )
                Text(
                    stringResource(R.string.menu_auto_updates_check_interval),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
                )
            }
            FlowRow(
                Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement =
                    Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CheckInterval.entries.forEachIndexed { index, interval ->
                    ToggleButton(
                        checked = selectedInterval == interval,
                        onCheckedChange = { checked -> if (checked) onSelect(interval) },
                        modifier =
                            Modifier.heightIn(min = 48.dp).semantics { role = Role.RadioButton },
                        enabled = enabled,
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                CheckInterval.entries.lastIndex ->
                                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    ) {
                        if (selectedInterval == interval) {
                            Icon(
                                Icons.Rounded.Check,
                                null,
                                Modifier.size(ToggleButtonDefaults.IconSize),
                            )
                        }
                        Text(
                            intervalLabel(interval),
                            Modifier.padding(
                                start = if (selectedInterval == interval) 6.dp else 0.dp
                            ),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun intervalLabel(interval: CheckInterval) =
    stringResource(
        when (interval) {
            CheckInterval.DAILY -> R.string.time_unit_day
            CheckInterval.WEEKLY -> R.string.time_unit_week
            CheckInterval.MONTHLY -> R.string.time_unit_month
        }
    )

private fun installRecoveryScriptExists() = File("/vendor/bin/install-recovery.sh").exists()
