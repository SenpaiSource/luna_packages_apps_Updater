/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.updates.action

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.lineageos.updater.ui.UpdaterEmblem

data class AlertDialogState(
    val onConfirm: () -> Unit = {},
    val showDismiss: Boolean = false,
    val title: String,
    val text: AnnotatedString,
)

@Composable
fun UpdateActionDialog(dialog: AlertDialogState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { UpdaterEmblem(Icons.Rounded.SystemUpdate, Modifier.size(56.dp)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    dialog.onConfirm()
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton =
            if (dialog.showDismiss) {
                {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            } else null,
        title = { Text(dialog.title) },
        text = { Text(dialog.text) },
    )
}
