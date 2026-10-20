/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.updatescheck

import android.content.Context
import android.os.SystemClock
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SyncProblem
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.util.Date
import kotlinx.coroutines.delay
import org.lineageos.updater.R
import org.lineageos.updater.ui.UpdaterEmblem

private const val MIN_CHECKING_DURATION_MILLIS = 2_000L

sealed interface UpdatesCheckState {
    data object Idle : UpdatesCheckState

    data object Checking : UpdatesCheckState

    data object NoInternet : UpdatesCheckState

    data object Error : UpdatesCheckState
}

data class UpdatesCheckModel(
    val state: UpdatesCheckState,
    val lastCheckedTimestamp: Long,
    val canCheckForUpdates: Boolean,
)

class UpdatesCheckUiState internal constructor(internal val displayedState: UpdatesCheckState) {
    val isStatusVisible =
        when (displayedState) {
            UpdatesCheckState.Idle -> false
            UpdatesCheckState.Checking,
            UpdatesCheckState.NoInternet,
            UpdatesCheckState.Error -> true
        }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdatesCheck(
    model: UpdatesCheckModel,
    uiState: UpdatesCheckUiState,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val state = uiState.displayedState
    val isError = state == UpdatesCheckState.Error || state == UpdatesCheckState.NoInternet
    val isChecking = state == UpdatesCheckState.Checking
    val motionScheme = MaterialTheme.motionScheme
    val containerColor by
        animateColorAsState(
            if (isError) colors.errorContainer else colors.surfaceContainer,
            motionScheme.fastEffectsSpec(),
            label = "check status color",
        )
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = if (isError) colors.onErrorContainer else colors.onSurface,
    ) {
        Column(
            Modifier.animateContentSize(motionScheme.defaultSpatialSpec()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (fadeIn(motionScheme.fastEffectsSpec()) +
                        scaleIn(motionScheme.fastSpatialSpec(), initialScale = 0.96f)) togetherWith
                        (fadeOut(motionScheme.fastEffectsSpec()) +
                            scaleOut(motionScheme.fastSpatialSpec(), targetScale = 0.96f))
                },
                label = "check status",
            ) { displayedState ->
                val statusIsError =
                    displayedState == UpdatesCheckState.Error ||
                        displayedState == UpdatesCheckState.NoInternet
                val title =
                    stringResource(
                        when (displayedState) {
                            UpdatesCheckState.Idle -> R.string.updater_updates
                            UpdatesCheckState.Checking -> R.string.checking_for_updates
                            UpdatesCheckState.NoInternet -> R.string.check_your_internet_connection
                            UpdatesCheckState.Error -> R.string.updates_check_failed
                        }
                    )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (displayedState == UpdatesCheckState.Checking) {
                        ContainedLoadingIndicator(Modifier.size(56.dp))
                    } else {
                        UpdaterEmblem(
                            icon =
                                when (displayedState) {
                                    UpdatesCheckState.NoInternet -> Icons.Rounded.CloudOff
                                    UpdatesCheckState.Error -> Icons.Rounded.SyncProblem
                                    else -> Icons.Rounded.SystemUpdate
                                },
                            modifier = Modifier.size(56.dp),
                            containerColor =
                                if (statusIsError) colors.error else colors.primaryContainer,
                            contentColor =
                                if (statusIsError) colors.onError else colors.onPrimaryContainer,
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            title,
                            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.titleLargeEmphasized,
                        )
                        if (displayedState == UpdatesCheckState.Idle) {
                            Text(
                                stringResource(R.string.updater_updates_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (model.canCheckForUpdates && !isChecking) {
                Button(
                    onClick = onCheckClick,
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(min = ButtonDefaults.MediumContainerHeight),
                    shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                    contentPadding =
                        ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                ) {
                    Icon(Icons.Rounded.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.check_for_updates),
                        style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
                    )
                }
            }
            Text(
                if (model.lastCheckedTimestamp > 0L) {
                    formatLastCheckedText(context, model.lastCheckedTimestamp)
                } else {
                    stringResource(R.string.updater_never_checked)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) colors.onErrorContainer else colors.onSurfaceVariant,
            )
        }
    }
}

/** Keeps the checking state visible long enough for the progress animation to be readable. */
@Composable
private fun rememberStateWithMinimumCheckingDuration(state: UpdatesCheckState): UpdatesCheckState {
    var displayedState by remember { mutableStateOf(state) }
    var checkingStartedAtMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state) {
        if (state == UpdatesCheckState.Checking) {
            checkingStartedAtMillis = SystemClock.elapsedRealtime()
            displayedState = state
            return@LaunchedEffect
        }

        if (displayedState == UpdatesCheckState.Checking) {
            val elapsed = SystemClock.elapsedRealtime() - checkingStartedAtMillis
            val remaining = MIN_CHECKING_DURATION_MILLIS - elapsed
            if (remaining > 0L) delay(remaining)
        }

        displayedState = state
    }

    return displayedState
}

@Composable
internal fun rememberUpdatesCheckUiState(state: UpdatesCheckState): UpdatesCheckUiState {
    val displayedState = rememberStateWithMinimumCheckingDuration(state)
    return remember(displayedState) { UpdatesCheckUiState(displayedState) }
}

/**
 * Formats the last checked time.
 *
 * Today's checks show only the time. Older checks show both date and time.
 */
private fun formatLastCheckedText(context: Context, timestampMillis: Long): String {
    val time = DateFormat.getTimeFormat(context).format(Date(timestampMillis))

    if (DateUtils.isToday(timestampMillis)) {
        return context.getString(R.string.header_last_updates_check_time, time)
    }

    val date =
        DateUtils.formatDateTime(
            context,
            timestampMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
        )

    return context.getString(R.string.header_last_updates_check, date, time)
}
