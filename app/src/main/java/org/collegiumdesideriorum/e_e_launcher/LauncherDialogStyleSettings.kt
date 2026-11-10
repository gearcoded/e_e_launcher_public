package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LauncherDialogStyleSettings(
    value: LauncherDialogStyle,
    onValueChange: (LauncherDialogStyle) -> Unit,
    onChooseImage: () -> Unit,
    onDraggingChange: (Boolean) -> Unit
) {
    SettingsSectionTitle(l10n(R.string.ui_dialog_background))
    LauncherInterfaceBackgroundMode.values().forEach { mode ->
        SimpleRadioOption(mode.title, mode == value.backgroundMode) {
            onValueChange(value.copy(
                backgroundMode = mode,
                opacityPercent = if (mode == LauncherInterfaceBackgroundMode.NONE) {
                    value.opacityPercent
                } else value.opacityPercent.coerceAtLeast(35)
            ))
        }
    }
    if (value.backgroundMode != LauncherInterfaceBackgroundMode.NONE) {
        VisualColorSetting(l10n(R.string.ui_primary_colour), value.colorAHex) {
            onValueChange(value.copy(colorAHex = it))
        }
        if (value.backgroundMode == LauncherInterfaceBackgroundMode.GRADIENT) {
            VisualColorSetting(l10n(R.string.ui_secondary_colour), value.colorBHex) {
                onValueChange(value.copy(colorBHex = it))
            }
        }
        if (value.backgroundMode == LauncherInterfaceBackgroundMode.IMAGE) {
            Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth()) {
                Text(if (value.imageUri.isNullOrBlank()) l10n(R.string.ui_choose_picture) else l10n(R.string.ui_replace_picture))
            }
        }
        IntegerSliderSetting(
            l10n(R.string.ui_opacity), value.opacityPercent, 0, 100,
            onDraggingChange = onDraggingChange
        ) { onValueChange(value.copy(opacityPercent = it)) }
        IntegerSliderSetting(
            l10n(R.string.ui_picture_blur_dp), value.blurDp, 0, 48,
            onDraggingChange = onDraggingChange
        ) { onValueChange(value.copy(blurDp = it)) }
    }
    SettingsSectionTitle(l10n(R.string.ui_dialog_text))
    VisualColorSetting(l10n(R.string.ui_text_colour), value.textColorHex) {
        onValueChange(value.copy(textColorHex = it))
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(l10n(R.string.ui_show_shadow))
            Text(l10n(R.string.ui_separate_shadow_for_dialog_text))
        }
        Switch(
            checked = value.textShadowEnabled,
            onCheckedChange = { onValueChange(value.copy(textShadowEnabled = it)) }
        )
    }
    if (value.textShadowEnabled) {
        IntegerSliderSetting(l10n(R.string.ui_shadow_offset_dp), value.textShadowOffsetDp, 0, 12) {
            onValueChange(value.copy(textShadowOffsetDp = it))
        }
        IntegerSliderSetting(l10n(R.string.ui_shadow_blur_dp), value.textShadowBlurDp, 0, 24) {
            onValueChange(value.copy(textShadowBlurDp = it))
        }
        VisualColorSetting(l10n(R.string.ui_shadow_colour), value.textShadowColorHex) {
            onValueChange(value.copy(textShadowColorHex = it))
        }
        IntegerSliderSetting(l10n(R.string.ui_shadow_opacity), value.textShadowOpacityPercent, 0, 100) {
            onValueChange(value.copy(textShadowOpacityPercent = it))
        }
    }
}
