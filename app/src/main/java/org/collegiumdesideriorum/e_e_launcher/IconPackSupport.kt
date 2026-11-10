package org.collegiumdesideriorum.e_e_launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

internal data class IconPackDescriptor(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?
)

internal data class IconPackEntry(
    val drawableName: String,
    val componentName: String?
)

/**
 * Reads the de-facto appfilter.xml format used by Android icon packs.
 * The format is not an Android platform API, so malformed packs are ignored safely.
 */
internal object IconPackRepository {
    private val themeActions = listOf(
        "org.adw.launcher.THEMES",
        "com.gau.go.launcherex.theme",
        "com.novalauncher.THEME",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "org.collegiumdesideriorum.e_e_launcher.ICON_PACK"
    )

    fun discover(context: Context): List<IconPackDescriptor> {
        val pm = context.packageManager
        val candidates = linkedSetOf<String>()

        themeActions.forEach { action ->
            runCatching {
                pm.queryIntentActivities(Intent(action), PackageManager.MATCH_ALL)
                    .mapNotNullTo(candidates) { it.activityInfo?.packageName }
            }
        }

        // Some packs only expose a normal launcher activity and appfilter.xml.
        runCatching {
            pm.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.MATCH_ALL
            ).mapNotNullTo(candidates) { it.activityInfo?.packageName }
        }

        return candidates.asSequence()
            .filter { it != context.packageName }
            .filter { hasAppFilter(context, it) }
            .mapNotNull { packageName ->
                runCatching {
                    val info = pm.getApplicationInfo(packageName, 0)
                    IconPackDescriptor(
                        packageName = packageName,
                        label = info.loadLabel(pm).toString().ifBlank { packageName },
                        icon = info.loadIcon(pm)?.toBitmap(112, 112)?.asImageBitmap()
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun loadEntries(context: Context, packageName: String): List<IconPackEntry> =
        parseAppFilter(context, packageName)
            .distinctBy { it.componentName to it.drawableName }
            .take(10_000)

    fun loadIcon(context: Context, packageName: String, drawableName: String): ImageBitmap? {
        if (drawableName.isBlank()) return null
        return runCatching {
            val resources = context.packageManager.getResourcesForApplication(packageName)
            val id = resources.getIdentifier(drawableName, "drawable", packageName)
                .takeIf { it != 0 }
                ?: resources.getIdentifier(drawableName, "mipmap", packageName).takeIf { it != 0 }
                ?: return null
            resources.getDrawable(id, null)
                .toBitmap(192, 192)
                .asImageBitmap()
        }.getOrNull()
    }

    fun matchingEntry(
        entries: List<IconPackEntry>,
        componentName: ComponentName
    ): IconPackEntry? {
        val flattened = componentName.flattenToString()
        return entries.firstOrNull { it.componentName == flattened }
            ?: entries.firstOrNull {
                it.componentName?.substringBefore('/') == componentName.packageName
            }
    }

    private fun hasAppFilter(context: Context, packageName: String): Boolean = runCatching {
        val resources = context.packageManager.getResourcesForApplication(packageName)
        if (resources.getIdentifier("appfilter", "xml", packageName) != 0) return@runCatching true
        context.createPackageContext(packageName, 0).assets.open("appfilter.xml").use { true }
    }.getOrDefault(false)

    private fun parseAppFilter(context: Context, packageName: String): List<IconPackEntry> {
        val fromResource: List<IconPackEntry> = runCatching<List<IconPackEntry>> {
            val resources = context.packageManager.getResourcesForApplication(packageName)
            val id = resources.getIdentifier("appfilter", "xml", packageName)
            if (id == 0) emptyList<IconPackEntry>() else resources.getXml(id).useParser(::readItems)
        }.getOrDefault(emptyList())
        if (fromResource.isNotEmpty()) return fromResource

        return runCatching {
            context.createPackageContext(packageName, 0).assets.open("appfilter.xml").use { input: java.io.InputStream ->
                val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
                    setInput(input, "UTF-8")
                }
                readItems(parser)
            }
        }.getOrDefault(emptyList())
    }

    private inline fun <T> XmlResourceParser.useParser(block: (XmlPullParser) -> T): T =
        try {
            block(this)
        } finally {
            close()
        }

    private fun readItems(parser: XmlPullParser): List<IconPackEntry> {
        val result = ArrayList<IconPackEntry>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name.equals("item", ignoreCase = true)) {
                val drawable = parser.getAttributeValue(null, "drawable")?.trim().orEmpty()
                if (drawable.isNotBlank()) {
                    val rawComponent = parser.getAttributeValue(null, "component")
                    result += IconPackEntry(drawable, normalizeComponent(rawComponent))
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun normalizeComponent(raw: String?): String? {
        val cleaned = raw?.trim()
            ?.removePrefix("ComponentInfo{")
            ?.removeSuffix("}")
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return ComponentName.unflattenFromString(cleaned)?.flattenToString() ?: cleaned
    }
}

@Composable
internal fun IconPackPickerDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onSelect: (packageName: String, drawableName: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var packs by remember { mutableStateOf<List<IconPackDescriptor>?>(null) }
    var selectedPack by remember { mutableStateOf<IconPackDescriptor?>(null) }
    var entries by remember { mutableStateOf<List<IconPackEntry>?>(null) }

    LaunchedEffect(Unit) {
        packs = withContext(Dispatchers.IO) { IconPackRepository.discover(context.applicationContext) }
    }
    LaunchedEffect(selectedPack?.packageName) {
        val pack = selectedPack
        entries = if (pack == null) null else withContext(Dispatchers.IO) {
            val all = IconPackRepository.loadEntries(context.applicationContext, pack.packageName)
            val match = IconPackRepository.matchingEntry(all, app.componentName)
            if (match == null) all else listOf(match) + all.filterNot { it.drawableName == match.drawableName }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(selectedPack?.let { l10n(R.string.ui_choose_an_icon, it.label) } ?: l10n(R.string.ui_choose_icon_pack))
        },
        text = {
            when {
                selectedPack == null && packs == null -> Box(
                    Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                selectedPack == null -> {
                    val available = packs.orEmpty()
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (available.isEmpty()) {
                            Text(
                                l10n(R.string.ui_no_compatible_icon_packs_found_packs_with_appfilter_xml),
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }
                        available.forEach { pack ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { selectedPack = pack },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (pack.icon != null) Image(pack.icon, pack.label, Modifier.size(52.dp))
                                    else Box(
                                        Modifier.size(52.dp).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("IP") }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(pack.label, fontWeight = FontWeight.Bold)
                                        Text(
                                            pack.packageName,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.52f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                entries == null -> Box(
                    Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                else -> {
                    val pack = selectedPack!!
                    val icons = entries.orEmpty()
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            l10n(R.string.ui_the_first_icon_is_the_recommended_match_for_when, app.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.62f)
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxWidth().height(460.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(icons, key = { it.drawableName }) { entry ->
                                IconPackEntryCell(
                                    packPackage = pack.packageName,
                                    entry = entry,
                                    onClick = { onSelect(pack.packageName, entry.drawableName) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedPack != null) {
                TextButton(onClick = { selectedPack = null; entries = null }) { Text(l10n(R.string.ui_back_to_pack_list)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun IconPackEntryCell(
    packPackage: String,
    entry: IconPackEntry,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var icon by remember(packPackage, entry.drawableName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(packPackage, entry.drawableName) {
        icon = withContext(Dispatchers.IO) {
            IconPackRepository.loadIcon(context.applicationContext, packPackage, entry.drawableName)
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.055f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) Image(icon!!, entry.drawableName, Modifier.size(56.dp))
            else Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) }
            Spacer(Modifier.height(4.dp))
            Text(
                entry.drawableName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/** Generic icon-pack picker for launcher items that are not tied to an installed app. */
@Composable
internal fun IconPackAssetPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelect: (packageName: String, drawableName: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var packs by remember { mutableStateOf<List<IconPackDescriptor>?>(null) }
    var selectedPack by remember { mutableStateOf<IconPackDescriptor?>(null) }
    var entries by remember { mutableStateOf<List<IconPackEntry>?>(null) }

    LaunchedEffect(Unit) {
        packs = withContext(Dispatchers.IO) { IconPackRepository.discover(context.applicationContext) }
    }
    LaunchedEffect(selectedPack?.packageName) {
        val pack = selectedPack
        entries = if (pack == null) null else withContext(Dispatchers.IO) {
            IconPackRepository.loadEntries(context.applicationContext, pack.packageName)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(selectedPack?.let { "${it.label}: $title" } ?: title) },
        text = {
            when {
                selectedPack == null && packs == null -> Box(
                    Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                selectedPack == null -> Column(
                    Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val available = packs.orEmpty()
                    if (available.isEmpty()) {
                        Text(l10n(R.string.ui_no_compatible_icon_packs_found), color = Color.White.copy(alpha = 0.68f))
                    }
                    available.forEach { pack ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedPack = pack },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                        ) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (pack.icon != null) Image(pack.icon, pack.label, Modifier.size(52.dp))
                                else Box(
                                    Modifier.size(52.dp).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text("IP") }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(pack.label, fontWeight = FontWeight.Bold)
                                    Text(pack.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.52f))
                                }
                            }
                        }
                    }
                }

                entries == null -> Box(
                    Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                else -> {
                    val pack = selectedPack!!
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth().height(460.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entries.orEmpty(), key = { it.drawableName }) { entry ->
                            IconPackEntryCell(
                                packPackage = pack.packageName,
                                entry = entry,
                                onClick = { onSelect(pack.packageName, entry.drawableName) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedPack != null) {
                TextButton(onClick = { selectedPack = null; entries = null }) { Text(l10n(R.string.ui_back_to_pack_list)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}
