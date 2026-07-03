/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.deviceinfo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.android.settingslib.spa.debug.UiModePreviews
import com.android.settingslib.spa.framework.theme.SettingsDimension
import com.android.settingslib.spa.framework.theme.SettingsShape.CornerExtraLarge1
import com.android.settingslib.spa.framework.theme.SettingsSpace
import com.android.settingslib.spa.framework.theme.SettingsTheme
import org.lineageos.updater.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

private const val WALLPAPER_SCRIM_ALPHA = 0.4f
private const val WALLPAPER_BLUR_DP = 30
private const val PICSUM_WIDTH = 480
private const val PICSUM_HEIGHT = 480
private const val NETWORK_TIMEOUT_MS = 8_000
private const val BITMAP_CACHE_SIZE = 4
private const val WALLPAPER_FADE_IN_MS = 450
private const val WALLPAPER_ROTATE_INTERVAL_MS = 60_000L

private val bitmapCache = LruCache<String, Bitmap>(BITMAP_CACHE_SIZE)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdaterCard(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
    shape: Shape = CornerExtraLarge1,
    maintainer: String? = null,
    device: String? = null,
) {
    val onBrandColor = colorResource(R.color.on_brand_surface)
    var picsumSeed by rememberSaveable { mutableIntStateOf(Random.nextInt(Int.MAX_VALUE)) }
    val wallpaperUrl = "https://picsum.photos/seed/$picsumSeed/$PICSUM_WIDTH/$PICSUM_HEIGHT"

    LaunchedEffect(Unit) {
        while (true) {
            delay(WALLPAPER_ROTATE_INTERVAL_MS)
            picsumSeed = Random.nextInt(Int.MAX_VALUE)
        }
    }

    val density = LocalDensity.current
    val displayLarge = MaterialTheme.typography.displayLarge

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = onBrandColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape),
        ) {
            var displayedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            var incomingBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            val crossfadeAlpha = remember { Animatable(0f) }

            LaunchedEffect(wallpaperUrl) {
                val bitmap = bitmapCache.get(wallpaperUrl)?.asImageBitmap()
                    ?: fetchBitmap(wallpaperUrl)?.also {
                        bitmapCache.put(wallpaperUrl, it)
                    }?.asImageBitmap()
                
                bitmap ?: return@LaunchedEffect

                incomingBitmap = bitmap
                crossfadeAlpha.snapTo(0f)
                crossfadeAlpha.animateTo(1f, animationSpec = tween(WALLPAPER_FADE_IN_MS))
                displayedBitmap = bitmap
                incomingBitmap = null
                crossfadeAlpha.snapTo(1f)
            }

            if (displayedBitmap == null && incomingBitmap == null) {
                Box(modifier = Modifier.matchParentSize().background(Color.DarkGray))
            }

            displayedBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(radius = WALLPAPER_BLUR_DP.dp),
                )
            }

            incomingBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { this.alpha = crossfadeAlpha.value }
                        .blur(radius = WALLPAPER_BLUR_DP.dp),
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = WALLPAPER_SCRIM_ALPHA)),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SettingsDimension.paddingLarge)
                        .semantics(mergeDescendants = true) {},
                ) {

                    Column(modifier = Modifier.alignByBaseline()) {
                        Text(
                            text = stringResource(R.string.header_build_version, buildVersion),
                            style = MaterialTheme.typography.displayMediumEmphasized,
                        )
                        val bylineText = if (!maintainer.isNullOrBlank()) {
                            stringResource(R.string.updater_maintainer_by, maintainer)
                        } else {
                            stringResource(R.string.updater_build_unofficial)
                        }
                        Text(
                            text = bylineText,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = onBrandColor.copy(alpha = 0.9f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SettingsSpace.medium5))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = SettingsDimension.paddingLarge,
                            vertical = SettingsDimension.paddingLarge,
                        ),
                    verticalArrangement = Arrangement.spacedBy(SettingsDimension.paddingLarge),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(SettingsDimension.paddingLarge),
                    ) {
                        InfoColumn(
                            label = stringResource(R.string.header_android_version, androidVersion),
                            value = device.orEmpty(),
                            modifier = Modifier.weight(1f),
                        )
                        InfoColumn(
                            label = stringResource(R.string.build_date),
                            value = buildDate,
                            modifier = Modifier.weight(1f),
                        )
                        InfoColumn(
                            label = stringResource(R.string.security_update),
                            value = securityPatch,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        val bytes = connection.inputStream.use { it.readBytes() }

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

        var sampleSize = 1
        while (boundsOptions.outWidth / (sampleSize * 2) >= PICSUM_WIDTH &&
            boundsOptions.outHeight / (sampleSize * 2) >= PICSUM_HEIGHT
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    } catch (e: IOException) {
        null
    } finally {
        connection?.disconnect()
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SettingsSpace.extraSmall2),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Normal,
            ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@UiModePreviews
@Composable
private fun UpdaterCardPreview() {
    SettingsTheme {
        UpdaterCard(
            buildVersion = "12.11",
            androidVersion = "16",
            buildDate = "Feb 20",
            securityPatch = "Feb 2026",
            modifier = Modifier.padding(SettingsDimension.itemPadding),
            maintainer = "neobuddy89",
            device = "OnePlus 12",
        )
    }
}
