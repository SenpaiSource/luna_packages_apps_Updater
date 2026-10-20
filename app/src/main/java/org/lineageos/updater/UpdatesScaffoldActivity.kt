/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.updater.controller.UpdaterController
import org.lineageos.updater.data.DeviceMetadata
import org.lineageos.updater.data.Update
import org.lineageos.updater.data.UpdateStatus
import org.lineageos.updater.deviceinfo.DeviceInfoBanner
import org.lineageos.updater.deviceinfo.actions.DeviceInfoActionButtons
import org.lineageos.updater.preferences.PreferencesActivity
import org.lineageos.updater.ui.UpdaterActionRow
import org.lineageos.updater.ui.UpdaterGroup
import org.lineageos.updater.ui.UpdaterScaffold
import org.lineageos.updater.ui.UpdaterTheme
import org.lineageos.updater.updates.UpdateList
import org.lineageos.updater.updates.action.AlertDialogState
import org.lineageos.updater.updates.action.UpdateActionDialog
import org.lineageos.updater.updates.action.UpdateActionHandler
import org.lineageos.updater.updates.state.UpdateItemStateMapper
import org.lineageos.updater.updatescheck.UpdatesCheck
import org.lineageos.updater.updatescheck.UpdatesCheckModel
import org.lineageos.updater.updatescheck.rememberUpdatesCheckUiState

abstract class UpdatesScaffoldActivity : ComponentActivity() {
    private val viewModel by viewModels<UpdatesViewModel>()
    private var activeUpdaterController: UpdaterController? by mutableStateOf(null)
    private var controllerStateVersion: Int by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    protected fun setupCompose() {
        setContent {
            UpdaterTheme {
                val uiState by viewModel.uiState.collectAsState()
                UpdatesScaffoldContent(
                    uiState = uiState,
                    updaterController = activeUpdaterController,
                    controllerStateVersion = controllerStateVersion,
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onRefreshClick = { onRefreshClick() },
                    onLocalUpdateClick = { onLocalUpdateClick() },
                    onPreferencesClick = {
                        startActivity(
                            Intent(this@UpdatesScaffoldActivity, PreferencesActivity::class.java)
                        )
                    },
                    onControllerStateChanged = { notifyControllerStateChanged() },
                )
            }
        }
    }

    protected fun setUpdaterController(controller: UpdaterController?) {
        activeUpdaterController = controller
        notifyControllerStateChanged()
    }

    protected fun notifyControllerStateChanged() {
        controllerStateVersion++
    }

    open fun onRefreshClick() {}

    open fun onLocalUpdateClick() {}

    open fun exportUpdate(update: Update) {}
}

@Composable
private fun UpdatesScaffoldContent(
    uiState: UpdatesViewModel.UiState,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    onBack: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onControllerStateChanged: () -> Unit,
) {
    val title = getTitleForUpdateStatus(uiState.updates)

    val actionDialogState = remember { mutableStateOf<AlertDialogState?>(null) }
    actionDialogState.value?.let { dialog ->
        UpdateActionDialog(dialog = dialog, onDismiss = { actionDialogState.value = null })
    }

    UpdaterScaffold(title = title, onBack = onBack) { paddingValues ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            if (isWideScreen()) {
                WideUpdatesScaffold(
                    paddingValues = paddingValues,
                    updatesCheckModel = uiState.updatesCheckModel,
                    updates = uiState.updates,
                    metadata = uiState.deviceMetadata,
                    updaterController = updaterController,
                    controllerStateVersion = controllerStateVersion,
                    showDialog = { actionDialogState.value = it },
                    onControllerStateChanged = onControllerStateChanged,
                    onRefreshClick = onRefreshClick,
                    onLocalUpdateClick = onLocalUpdateClick,
                    onPreferencesClick = onPreferencesClick,
                )
            } else {
                UpdatesScaffold(
                    paddingValues = paddingValues,
                    updatesCheckModel = uiState.updatesCheckModel,
                    updates = uiState.updates,
                    metadata = uiState.deviceMetadata,
                    updaterController = updaterController,
                    controllerStateVersion = controllerStateVersion,
                    showDialog = { actionDialogState.value = it },
                    onControllerStateChanged = onControllerStateChanged,
                    onRefreshClick = onRefreshClick,
                    onLocalUpdateClick = onLocalUpdateClick,
                    onPreferencesClick = onPreferencesClick,
                )
            }
        }
    }
}

@Composable
private fun WideUpdatesScaffold(
    paddingValues: PaddingValues,
    updatesCheckModel: UpdatesCheckModel,
    updates: List<Update>,
    metadata: DeviceMetadata,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    Row(
        modifier =
            Modifier.widthIn(max = 1200.dp)
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                )
    ) {
        UpdatesInformationPane(
            maintainer = metadata.maintainer,
            device = metadata.device,
            modifier =
                Modifier.weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        )
        UpdatesActionPane(
            model = updatesCheckModel,
            updates = updates,
            metadata = metadata,
            updaterController = updaterController,
            controllerStateVersion = controllerStateVersion,
            showDialog = showDialog,
            onControllerStateChanged = onControllerStateChanged,
            onRefreshClick = onRefreshClick,
            onLocalUpdateClick = onLocalUpdateClick,
            onPreferencesClick = onPreferencesClick,
            modifier =
                Modifier.weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        )
    }
}

@Composable
private fun UpdatesScaffold(
    paddingValues: PaddingValues,
    updatesCheckModel: UpdatesCheckModel,
    updates: List<Update>,
    metadata: DeviceMetadata,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    Column(
        Modifier.widthIn(max = 680.dp)
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        UpdatesInformationPane(maintainer = metadata.maintainer, device = metadata.device)
        UpdatesActionPane(
            model = updatesCheckModel,
            updates = updates,
            metadata = metadata,
            updaterController = updaterController,
            controllerStateVersion = controllerStateVersion,
            showDialog = showDialog,
            onControllerStateChanged = onControllerStateChanged,
            onRefreshClick = onRefreshClick,
            onLocalUpdateClick = onLocalUpdateClick,
            onPreferencesClick = onPreferencesClick,
        )
    }
}

@Composable
private fun UpdatesInformationPane(
    maintainer: String?,
    device: String?,
    modifier: Modifier = Modifier,
) {
    DeviceInfoBanner(
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        maintainer = maintainer,
        device = device,
    )
}

@Composable
private fun UpdatesActionPane(
    model: UpdatesCheckModel,
    updates: List<Update>,
    metadata: DeviceMetadata,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as UpdatesScaffoldActivity
    val networkMonitor = remember {
        (context.applicationContext as UpdaterApplication).networkMonitor
    }
    val networkState by
        networkMonitor.networkState.collectAsState(initial = networkMonitor.currentNetworkState)
    val userPreferencesRepository = remember {
        (context.applicationContext as UpdaterApplication).userPreferencesRepository
    }
    val streamUpdatesEnabled by
        userPreferencesRepository.streamUpdatesFlow.collectAsState(initial = true)

    val updateItems =
        remember(
            updates,
            updaterController,
            networkState,
            streamUpdatesEnabled,
            controllerStateVersion,
        ) {
            val controller = updaterController ?: return@remember emptyList()
            val mapper = UpdateItemStateMapper(context, controller, streamUpdatesEnabled)
            updates.mapNotNull { update ->
                controller.getUpdate(update.downloadId)?.let { mapper.map(it, networkState) }
            }
        }

    val actionHandler =
        remember(updaterController) {
            updaterController?.let { controller ->
                UpdateActionHandler(
                    activity = activity,
                    updaterController = controller,
                    exportUpdate = { update -> activity.exportUpdate(update) },
                    showDialog = showDialog,
                )
            }
        }
    val updatesCheckUiState = rememberUpdatesCheckUiState(model.state)

    Column(modifier = modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        UpdatesCheck(model = model, uiState = updatesCheckUiState, onCheckClick = onRefreshClick)
        UpdateList(
            items = updateItems,
            isUpdatesCheckStatusVisible = updatesCheckUiState.isStatusVisible,
            onAction = { action, downloadId ->
                val controller = updaterController ?: return@UpdateList
                val update = controller.getUpdate(downloadId) ?: return@UpdateList
                actionHandler?.perform(action, update)
                onControllerStateChanged()
            },
        )
        if (
            LocalConfiguration.current.uiMode and Configuration.UI_MODE_TYPE_MASK !=
                Configuration.UI_MODE_TYPE_TELEVISION
        ) {
            DeviceInfoActionButtons()
        }
        DeviceLinksRow(metadata = metadata)
        UpdatesFooter(
            onLocalUpdateClick = onLocalUpdateClick,
            onPreferencesClick = onPreferencesClick,
        )
    }
}

@Composable
private fun isWideScreen(): Boolean {
    val minWideScreenWidth = 840.dp
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize

    return with(density) { windowSize.width.toDp() >= minWideScreenWidth }
}

@Composable
private fun UpdatesFooter(onLocalUpdateClick: () -> Unit, onPreferencesClick: () -> Unit) {
    UpdaterGroup(title = stringResource(R.string.updater_tools)) {
        UpdaterActionRow(
            title = stringResource(R.string.local_update_import),
            summary = stringResource(R.string.local_update_import_summary),
            icon = Icons.Rounded.FolderOpen,
            onClick = onLocalUpdateClick,
        )
        UpdaterActionRow(
            title = stringResource(R.string.menu_preferences),
            summary = stringResource(R.string.preferences_summary),
            icon = Icons.Rounded.Tune,
            onClick = onPreferencesClick,
        )
    }
}

@Composable
private fun getTitleForUpdateStatus(updates: List<Update>): String =
    when {
        updates.any { it.status == UpdateStatus.UPDATED_NEED_REBOOT } ->
            stringResource(R.string.installing_update_finished)

        updates.any { it.status == UpdateStatus.INSTALLATION_FAILED } ->
            stringResource(R.string.installing_update_error)

        updates.any {
            it.status == UpdateStatus.INSTALLING || it.status == UpdateStatus.INSTALLATION_SUSPENDED
        } -> stringResource(R.string.installing_update)

        else -> stringResource(R.string.display_name)
    }
