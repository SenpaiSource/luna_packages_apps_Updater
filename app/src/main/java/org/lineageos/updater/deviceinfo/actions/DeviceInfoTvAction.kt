/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.deviceinfo.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import org.lineageos.updater.R
import org.lineageos.updater.ui.UpdaterTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeviceInfoTvAction(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.header_review_updates))
        Text(
            text = stringResource(R.string.header_download_url),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMediumEmphasized,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.found_bug))
        Spacer(modifier = Modifier.height(24.dp))
        QrCodeReportIssues()
    }
}

@UiModePreviews
@Composable
private fun DeviceInfoTvActionPreview() {
    UpdaterTheme { DeviceInfoTvAction() }
}
