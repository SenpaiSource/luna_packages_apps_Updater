/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: 2026 Lunaris AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package org.lineageos.updater.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import org.lineageos.updater.R
import org.lineageos.updater.ui.UpdaterEmblem
import org.lineageos.updater.ui.UpdaterTheme

@Composable
fun UpdaterCard(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
    maintainer: String? = null,
    device: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.brand_name),
                    modifier = Modifier.fillMaxWidth().semantics { heading() },
                    style = MaterialTheme.typography.displayMediumEmphasized,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            stringResource(R.string.updater_installed_version),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                        )
                        Text(buildVersion, style = MaterialTheme.typography.headlineSmallEmphasized)
                        Text(
                            if (!maintainer.isNullOrBlank())
                                stringResource(R.string.updater_maintainer_by, maintainer)
                            else stringResource(R.string.updater_build_unofficial),
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                    }
                    UpdaterEmblem(
                        Icons.Rounded.SystemUpdate,
                        Modifier.size(56.dp),
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surface.copy(alpha = 0.65f),
                    contentColor = colors.onSurface,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp).semantics(
                            mergeDescendants = true
                        ) {},
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.PhoneAndroid, null)
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (!device.isNullOrBlank()) {
                                Text(device, style = MaterialTheme.typography.titleMediumEmphasized)
                            }
                            Text(
                                stringResource(R.string.header_android_version, androidVersion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        Row(
            Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BuildDetail(
                stringResource(R.string.build_date),
                buildDate,
                Icons.Rounded.CalendarToday,
                Modifier.weight(1f),
            )
            BuildDetail(
                stringResource(R.string.security_update),
                securityPatch,
                Icons.Rounded.Security,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BuildDetail(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(20.dp).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.titleMediumEmphasized)
        }
    }
}

@UiModePreviews
@Preview(name = "Large text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun UpdaterCardPreview() {
    UpdaterTheme {
        UpdaterCard(
            "3.12",
            "16",
            "Oct 20",
            "Oct 2026",
            maintainer = "GhostRX89",
            device = "Nothing Phone (2)",
        )
    }
}
