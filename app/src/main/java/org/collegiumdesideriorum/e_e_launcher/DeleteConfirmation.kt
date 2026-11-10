package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(l10n(R.string.ui_delete), color = Color(0xFFFF7A83))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}
