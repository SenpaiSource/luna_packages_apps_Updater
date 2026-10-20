/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import org.lineageos.updater.data.DeviceMetadata
import org.lineageos.updater.ui.UpdaterActionSurface
import org.lineageos.updater.ui.UpdaterSectionTitle
import org.lineageos.updater.ui.UpdaterTheme

private data class DeviceLink(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val url: String,
)

@Composable
fun DeviceLinksRow(metadata: DeviceMetadata, modifier: Modifier = Modifier) {
    val links = buildList {
        if (metadata.telegram.isNotBlank())
            add(
                DeviceLink(
                    R.drawable.ic_link_telegram,
                    R.string.updater_link_telegram,
                    metadata.telegram,
                )
            )
        if (metadata.paypal.isNotBlank())
            add(
                DeviceLink(R.drawable.ic_link_donate, R.string.updater_link_donate, metadata.paypal)
            )
        metadata.forum?.takeIf(String::isNotBlank)?.let {
            add(DeviceLink(R.drawable.ic_link_forum, R.string.updater_link_forum, it))
        }
    }
    if (links.isEmpty()) return

    val context = LocalContext.current

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        UpdaterSectionTitle(stringResource(R.string.updater_community))
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            links.forEach { link ->
                LinkTile(
                    iconRes = link.iconRes,
                    labelRes = link.labelRes,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LinkTile(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UpdaterActionSurface(onClick, modifier.fillMaxHeight()) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                Modifier.size(48.dp),
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(painterResource(iconRes), null, Modifier.size(24.dp))
                }
            }
            Text(
                stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@UiModePreviews
@Composable
private fun DeviceLinksRowPreview() {
    UpdaterTheme {
        DeviceLinksRow(
            metadata =
                DeviceMetadata(
                    telegram = "https://t.me/example",
                    paypal = "https://www.paypal.com/paypalme/example",
                    forum = "https://forum.example/thread",
                )
        )
    }
}
