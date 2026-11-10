package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shared header for numeric settings. The label always gets the full available
 * width and may wrap naturally. The value sits on a separate line immediately
 * above the slider instead of squeezing a long translated label into one letter
 * per line on narrow dialogs or with large system font sizes.
 */
@Composable
internal fun SettingsSliderHeader(label: String, valueText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            softWrap = true
        )
        Text(
            text = valueText,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            softWrap = true
        )
    }
}
