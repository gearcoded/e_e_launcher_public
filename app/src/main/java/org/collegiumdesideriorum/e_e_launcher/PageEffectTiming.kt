package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

/** 0 keeps Compose Pager's default settling animation. Positive values affect the settle, not the drag. */
internal class PageEffectTimingSettings(private val context: Context) {
    private val preferences = context.getSharedPreferences("e_e_launcher_workspace", Context.MODE_PRIVATE)
    var workspaceMs by mutableIntStateOf(read("workspace_min_effect_ms"))
        private set
    var drawerMs by mutableIntStateOf(read("drawer_min_effect_ms"))
        private set

    private fun read(key: String): Int =
        (preferences.getInt(key, 0).coerceIn(0, 1000) / 10) * 10

    fun setWorkspace(ms: Int) {
        val normalized = normalize(ms)
        if (normalized != workspaceMs) {
            workspaceMs = normalized
            preferences.edit().putInt("workspace_min_effect_ms", normalized).apply()
        }
    }

    fun setDrawer(ms: Int) {
        val normalized = normalize(ms)
        if (normalized != drawerMs) {
            drawerMs = normalized
            preferences.edit().putInt("drawer_min_effect_ms", normalized).apply()
        }
    }

    private fun normalize(value: Int): Int = ((value.coerceIn(0, 1000) + 5) / 10) * 10
}

@Composable
internal fun MinimumPageEffectDurationSetting(
    title: String,
    defaultLabel: String,
    valueMs: Int,
    onDraggingChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSliderHeader(
            label = title,
            valueText = if (valueMs == 0) defaultLabel else "$valueMs ms"
        )
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = (valueMs.coerceIn(0, 1000) / 10).toFloat(),
            onValueChange = { onDraggingChange(true); onValueChange((it.roundToInt().coerceIn(0, 100)) * 10) },
            onValueChangeFinished = { onDraggingChange(false) },
            valueRange = 0f..100f,
            steps = 99
        )
    }
}
