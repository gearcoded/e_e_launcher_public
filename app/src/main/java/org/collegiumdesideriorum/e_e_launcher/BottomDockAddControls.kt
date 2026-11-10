package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BottomDockAddControls(
    addButtonEnabled: Boolean,
    onAddButtonEnabledChange: (Boolean) -> Unit,
    onAddToCurrentPage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onAddButtonEnabledChange(!addButtonEnabled) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(l10n(R.string.ui_button), style = MaterialTheme.typography.bodyLarge)
                Text(
                    l10n(R.string.ui_show_only_on_an_empty_bottom_dock_page),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f)
                )
            }
            Switch(
                checked = addButtonEnabled,
                onCheckedChange = onAddButtonEnabledChange
            )
        }
        Button(onClick = onAddToCurrentPage, modifier = Modifier.fillMaxWidth()) {
            Text(l10n(R.string.ui_add_item_to_current_page))
        }
    }
}

@Composable
internal fun BottomDockAddButton(
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("+", fontSize = 28.sp, color = textColor)
    }
}
