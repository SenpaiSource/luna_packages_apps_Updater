/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.deviceinfo

import android.content.res.Configuration
import android.icu.text.DateFormat
import android.icu.util.TimeZone
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.settingslib.spa.debug.UiModePreviews
import java.util.Date
import org.lineageos.updater.deviceinfo.actions.DeviceInfoTvAction
import org.lineageos.updater.ui.UpdaterTheme
import org.lineageos.updater.util.StringUtil

@Composable
fun DeviceInfoBanner(
    modifier: Modifier = Modifier,
    maintainer: String? = null,
    device: String? = null,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = remember(context, configuration.locales) { StringUtil.getCurrentLocale(context) }
    val buildVersion = remember { DeviceInfoUtils.buildVersion }
    val androidVersion = remember { DeviceInfoUtils.androidVersion }
    val deviceName = device?.takeIf { it.isNotBlank() } ?: DeviceInfoUtils.productName
    val buildDate =
        remember(locale) {
            DateFormat.getInstanceForSkeleton("MMMd", locale)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date(DeviceInfoUtils.buildDateTimestamp * 1000L))
        }
    val securityPatch =
        remember(locale) {
            StringUtil.formatSecurityPatch(context, DeviceInfoUtils.buildSecurityPatch)
        }

    DeviceInfoBanner(
        buildVersion = buildVersion,
        androidVersion = androidVersion,
        buildDate = buildDate,
        securityPatch = securityPatch,
        modifier = modifier,
        maintainer = maintainer,
        device = deviceName,
    )
}

@Composable
fun DeviceInfoBanner(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
    maintainer: String? = null,
    device: String? = null,
) {
    val uiMode = LocalConfiguration.current.uiMode
    val isTv = (uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION

    Column(modifier = modifier.fillMaxWidth()) {
        UpdaterCard(
            buildVersion = buildVersion,
            androidVersion = androidVersion,
            buildDate = buildDate,
            securityPatch = securityPatch,
            modifier = Modifier.fillMaxWidth(),
            maintainer = maintainer,
            device = device,
        )

        if (isTv) {
            DeviceInfoTvAction()
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_TELEVISION or Configuration.UI_MODE_NIGHT_YES,
)
@UiModePreviews
@Composable
private fun DeviceInfoBannerPreview() {
    UpdaterTheme {
        DeviceInfoBanner(
            buildVersion = "3.12",
            androidVersion = "16",
            buildDate = "Feb 20",
            securityPatch = "Feb 2026",
            maintainer = "GhostRX89",
            device = "Nothing Phone(2)",
        )
    }
}
