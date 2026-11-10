package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

internal data class PresetDockIcon(
    override val titleRes: Int,
    val action: String,
    val iconRes: Int
) : LocalizedTitle {
    val label: String get() = title
}

internal val presetDockIcons = listOf(
    PresetDockIcon(R.string.ui_phone, DOCK_ACTION_PHONE, R.drawable.ic_phone),
    PresetDockIcon(R.string.ui_files, DOCK_ACTION_FILES, R.drawable.ic_folder),
    PresetDockIcon(R.string.ui_applications, DOCK_ACTION_APPS, R.drawable.ic_apps),
    PresetDockIcon(R.string.ui_maps, DOCK_ACTION_MAPS, R.drawable.ic_map),
    PresetDockIcon(R.string.ui_messages, DOCK_ACTION_MESSAGES, R.drawable.ic_message)
)

internal fun dockActionIconRes(action: String?): Int =
    presetDockIcons.firstOrNull { it.action == action }?.iconRes ?: when (action) {
        "org.collegiumdesideriorum.e_e_launcher.action.APP_DRAWER" -> R.drawable.ic_apps
        "org.collegiumdesideriorum.e_e_launcher.action.SIDE_DOCK" -> R.drawable.ic_drag
        "org.collegiumdesideriorum.e_e_launcher.action.WORKSPACE_EFFECTS" -> R.drawable.ic_workspace_effect
        "org.collegiumdesideriorum.e_e_launcher.action.APP_DRAWER_EFFECTS" -> R.drawable.ic_app_drawer_effect
        "org.collegiumdesideriorum.e_e_launcher.action.ENVIRONMENT_EFFECTS" -> R.drawable.ic_environment
        else -> R.drawable.ic_settings
    }

@Composable
internal fun PresetDockIconSection(
    presets: List<PresetDockIcon>,
    onAdd: (PresetDockIcon) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(l10n(R.string.ui_preset_icons), Modifier.weight(1f))
            Text(if (expanded) "⌃" else "⌄")
        }
        if (expanded) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (presets.isEmpty()) {
                    Text(l10n(R.string.ui_all_icons_have_already_been_added), Modifier.padding(8.dp))
                }
                presets.forEach { preset ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onAdd(preset) }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(preset.iconRes),
                            contentDescription = preset.label,
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(preset.label)
                    }
                }
            }
        }
    }
}
