package org.collegiumdesideriorum.e_e_launcher

import android.appwidget.AppWidgetProviderInfo
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class WidgetProviderGroup(
    val packageName: String,
    val appLabel: String,
    val appIcon: ImageBitmap?,
    val providers: List<AppWidgetProviderInfo>
)

@Composable
internal fun AppWidgetPickerDialog(
    providers: List<AppWidgetProviderInfo>,
    displayMode: WidgetPickerDisplayMode,
    onDismiss: () -> Unit,
    onPick: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val densityDpi = context.resources.displayMetrics.densityDpi
    var expandedPackage by remember { mutableStateOf<String?>(null) }
    val groups = remember(providers) {
        providers
            .filter { it.widgetCategory and AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN != 0 }
            .filterNot { it.widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_HIDE_FROM_PICKER != 0 }
            .groupBy { it.provider.packageName }
            .map { (packageName, entries) ->
                val appInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
                val label = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: packageName
                val icon = appInfo?.let {
                    runCatching { packageManager.getApplicationIcon(it).toBitmap(96, 96).asImageBitmap() }.getOrNull()
                }
                WidgetProviderGroup(
                    packageName = packageName,
                    appLabel = label,
                    appIcon = icon,
                    providers = entries.sortedBy {
                        runCatching { it.loadLabel(packageManager).toString().lowercase() }.getOrDefault("")
                    }
                )
            }
            .sortedBy { it.appLabel.lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_choose_widget, displayMode.title)) },
        text = {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = 540.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (groups.isEmpty()) {
                    item { Text(l10n(R.string.ui_no_installed_widgets_found), color = Color.White.copy(alpha = 0.65f)) }
                }
                items(groups, key = { it.packageName }) { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                expandedPackage = if (expandedPackage == group.packageName) null else group.packageName
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (group.appIcon != null) Image(group.appIcon, group.appLabel, Modifier.size(44.dp))
                                else WidgetFallbackIcon(44)
                                Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                                    Text(group.appLabel, fontWeight = FontWeight.Bold)
                                    Text(l10n(R.string.ui_widget_s, group.providers.size), color = Color.White.copy(alpha = 0.56f))
                                }
                                Text(if (expandedPackage == group.packageName) "⌃" else "⌄")
                            }
                        }
                        if (expandedPackage == group.packageName) {
                            group.providers.forEach { info ->
                                WidgetProviderCard(info, displayMode, densityDpi, onPick)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun WidgetProviderCard(
    info: AppWidgetProviderInfo,
    displayMode: WidgetPickerDisplayMode,
    densityDpi: Int,
    onPick: (AppWidgetProviderInfo) -> Unit
) {
    val context = LocalContext.current
    val label = remember(info.provider) {
        runCatching { info.loadLabel(context.packageManager).toString() }.getOrDefault(info.provider.className)
    }
    val icon = remember(info.provider) {
        runCatching { info.loadIcon(context, densityDpi)?.toBitmap(128, 128)?.asImageBitmap() }.getOrNull()
    }
    val preview = remember(info.provider, displayMode) {
        if (displayMode != WidgetPickerDisplayMode.PREVIEW) null else runCatching {
            info.loadPreviewImage(context, densityDpi)?.let { drawable ->
                val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(640) ?: 420
                val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(360) ?: 220
                drawable.toBitmap(width, height).asImageBitmap()
            }
        }.getOrNull()
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 14.dp).clickable { onPick(info) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.045f))
    ) {
        when (displayMode) {
            WidgetPickerDisplayMode.LIST -> Row(
                Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) Image(icon, label, Modifier.size(44.dp)) else WidgetFallbackIcon(44)
                Column(Modifier.weight(1f).padding(start = 11.dp)) {
                    Text(label, fontWeight = FontWeight.Bold)
                    Text(l10n(R.string.ui_widget_dimensions_dp, info.minWidth, info.minHeight), color = Color.White.copy(alpha = 0.55f))
                }
            }
            WidgetPickerDisplayMode.ICON -> Column(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) Image(icon, label, Modifier.size(72.dp)) else WidgetFallbackIcon(72)
                Spacer(Modifier.height(7.dp))
                Text(label, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            WidgetPickerDisplayMode.PREVIEW -> Column(Modifier.fillMaxWidth().padding(10.dp)) {
                Box(
                    Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        preview != null -> Image(preview, label, Modifier.fillMaxSize().padding(7.dp))
                        icon != null -> Image(icon, label, Modifier.size(78.dp))
                        else -> Text(l10n(R.string.ui_preview_unavailable))
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(label, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WidgetFallbackIcon(sizeDp: Int) {
    Box(
        Modifier.size(sizeDp.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) { Text("W") }
}
