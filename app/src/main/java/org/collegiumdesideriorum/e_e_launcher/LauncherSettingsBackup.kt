package org.collegiumdesideriorum.e_e_launcher

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object LauncherSettingsBackup {
    private const val PREFS_NAME = "e_e_launcher_workspace"
    private const val ADVANCED_PREFS_NAME = "e_e_launcher_advanced"
    private const val WIDGET_RESTORE_PREFS_NAME = "e_e_launcher_widget_restore"
    private const val FORMAT = "e_e_launcher_settings"
    private const val KEY_WIDGETS = "widgets"
    private const val KEY_NEW_APP_CUTOFF = "new_app_cutoff_millis"
    private const val KEY_NEW_APP_CUTOFF_MANUAL = "new_app_cutoff_manual"
    private const val KEY_NEW_APP_CUTOFF_UPDATE = "new_app_cutoff_launcher_update"
    private const val KEY_PENDING_WIDGET_RESTORE = "pending_widget_restore"
    private val BACKUP_FILE_NAME_TIME_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT)

    private data class WidgetReference(
        val id: String,
        val appWidgetId: Int,
        val provider: String
    )

    fun defaultFileName(now: LocalDateTime = LocalDateTime.now()): String =
        "e_e_launcher_settings_${now.format(BACKUP_FILE_NAME_TIME_FORMAT)}.json"

    fun write(context: Context, uri: Uri) {
        val values = JSONObject()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).all.forEach { (key, value) ->
            val item = JSONObject()
            when (value) {
                is String -> item.put("type", "string").put("value", value)
                is Int -> item.put("type", "int").put("value", value)
                is Long -> item.put("type", "long").put("value", value)
                is Float -> item.put("type", "float").put("value", value.toDouble())
                is Boolean -> item.put("type", "boolean").put("value", value)
                is Set<*> -> item.put("type", "strings").put("value", JSONArray(value.filterIsInstance<String>()))
                else -> return@forEach
            }
            values.put(key, item)
        }
        val advancedValues = JSONObject()
        context.getSharedPreferences(ADVANCED_PREFS_NAME, Context.MODE_PRIVATE).all.forEach { (key, value) ->
            when (value) {
                is String -> advancedValues.put(key, JSONObject().put("type", "string").put("value", value))
                is Boolean -> advancedValues.put(key, JSONObject().put("type", "boolean").put("value", value))
            }
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", 3)
            .put("package", context.packageName)
            .put("preferences", values)
            .put("advancedPreferences", advancedValues)
            // appWidgetId belongs to the current AppWidgetHost allocation and is not
            // stable across reinstall/restore.  Version 3 additionally records the
            // launcher's own stable WorkspaceWidget.id so options can follow a widget
            // when Android assigns a different appWidgetId during rebinding.
            .put("widgetOptions", collectWidgetOptions(context))
        context.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(root.toString(2)) }
    }

    /**
     * Validate the entire backup before touching persistent state. SharedPreferences
     * does not offer transactions across three files, so failed commits also trigger
     * a best-effort rollback of all three preference snapshots.
     *
     * Provider calls and note-widget updates are deliberately postponed until all
     * persistent records have been committed successfully.
     */
    fun restore(context: Context, uri: Uri) {
        val root = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { JSONObject(it.readText()) }
            ?: error("Cannot open backup")
        require(root.optString("format") == FORMAT) { "Unsupported backup format" }
        require(root.optString("package") == context.packageName) { "Backup belongs to another application" }
        val version = root.opt("version") as? Number
            ?: error("Missing backup version")
        require(version.toInt() in 1..3 && version.toDouble() == version.toInt().toDouble()) {
            "Unsupported backup version"
        }

        // No commits or updates to hosted widgets before this entire section passes.
        val restoredValues = parsePreferences(root.getJSONObject("preferences"), advanced = false)
        val restoredAdvanced = if (root.has("advancedPreferences")) {
            parsePreferences(root.getJSONObject("advancedPreferences"), advanced = true)
        } else null // Historic backups without this section must preserve current advanced settings.
        val widgetOptions = when {
            !root.has("widgetOptions") -> null
            root.opt("widgetOptions") is JSONArray -> root.getJSONArray("widgetOptions")
            else -> error("Invalid widget options")
        }
        validateWidgetOptions(widgetOptions)
        val restoredWidgetReferences = parseWidgetReferences(restoredValues[KEY_WIDGETS] as? String)
        val widgetPlan = planWidgetRestore(context, restoredWidgetReferences, widgetOptions)

        if (!restoredValues.containsKey(KEY_NEW_APP_CUTOFF)) {
            val now = System.currentTimeMillis()
            val launcherUpdate = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            }.getOrDefault(now)
            restoredValues[KEY_NEW_APP_CUTOFF] = now
            restoredValues[KEY_NEW_APP_CUTOFF_MANUAL] = false
            restoredValues[KEY_NEW_APP_CUTOFF_UPDATE] = launcherUpdate
        }

        val workspacePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val advancedPrefs = context.getSharedPreferences(ADVANCED_PREFS_NAME, Context.MODE_PRIVATE)
        val restorePrefs = context.getSharedPreferences(WIDGET_RESTORE_PREFS_NAME, Context.MODE_PRIVATE)
        val snapshots: List<Map<String, Any>> = listOf(workspacePrefs, advancedPrefs, restorePrefs).map { prefs ->
            // SharedPreferences.all can expose mutable sets; copy them for rollback.
            prefs.all.mapNotNull { (key, value) ->
                value?.let { key to (if (it is Set<*>) it.filterIsInstance<String>().toSet() else it) }
            }.toMap()
        }
        try {
            check(writePreferences(workspacePrefs, restoredValues, clear = true)) {
                "Could not restore preferences"
            }
            if (restoredAdvanced != null) {
                check(writePreferences(advancedPrefs, restoredAdvanced, clear = true)) {
                    "Could not restore advanced preferences"
                }
            }
            check(savePendingWidgetRestoreRecords(context, widgetPlan.pending)) {
                "Could not save widget restore queue"
            }
        } catch (failure: Exception) {
            val rollbackErrors = mutableListOf<Throwable>()
            listOf(workspacePrefs, advancedPrefs, restorePrefs).forEachIndexed { index, prefs ->
                runCatching { check(writePreferences(prefs, snapshots[index], clear = true)) }
                    .exceptionOrNull()?.let(rollbackErrors::add)
            }
            rollbackErrors.forEach(failure::addSuppressed)
            throw failure
        }
        // A broken third-party provider must not turn an otherwise valid restore
        // into a partial, reported failure. The binding can be retried manually.
        widgetPlan.bound.forEach { (widget, values) ->
            runCatching { applyWidgetValues(context, widget, values) }
        }
    }

    private fun parsePreferences(source: JSONObject, advanced: Boolean): MutableMap<String, Any> {
        val result = linkedMapOf<String, Any>()
        source.keys().forEach { key ->
            val item = source.getJSONObject(key)
            val value = item.get("value")
            val decoded: Any = when (item.getString("type")) {
                "string" -> value as? String ?: error("Invalid string preference: $key")
                "boolean" -> value as? Boolean ?: error("Invalid boolean preference: $key")
                "int" -> {
                    require(!advanced) { "Unsupported advanced preference type: $key" }
                    val number = value as? Number ?: error("Invalid integer preference: $key")
                    val long = number.toLong()
                    require(long in Int.MIN_VALUE..Int.MAX_VALUE && number.toDouble() == long.toDouble()) {
                        "Integer preference out of range: $key"
                    }
                    long.toInt()
                }
                "long" -> {
                    require(!advanced) { "Unsupported advanced preference type: $key" }
                    val number = value as? Number ?: error("Invalid long preference: $key")
                    require(number.toDouble().isFinite() && number.toDouble() == number.toLong().toDouble()) {
                        "Invalid long preference: $key"
                    }
                    number.toLong()
                }
                "float" -> {
                    require(!advanced) { "Unsupported advanced preference type: $key" }
                    val number = value as? Number ?: error("Invalid float preference: $key")
                    require(number.toDouble().isFinite() && number.toDouble() in -Float.MAX_VALUE.toDouble()..Float.MAX_VALUE.toDouble()) {
                        "Invalid float preference: $key"
                    }
                    number.toFloat()
                }
                "strings" -> {
                    require(!advanced) { "Unsupported advanced preference type: $key" }
                    val array = value as? JSONArray ?: error("Invalid string set: $key")
                    buildSet {
                        for (index in 0 until array.length()) {
                            add(array.get(index) as? String ?: error("Invalid string set member: $key"))
                        }
                    }
                }
                else -> error("Unsupported preference type: $key")
            }
            result[key] = decoded
        }
        return result
    }

    private fun writePreferences(prefs: SharedPreferences, entries: Map<String, Any>, clear: Boolean): Boolean {
        val editor = prefs.edit()
        if (clear) editor.clear()
        entries.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                else -> error("Unsupported value type: $key")
            }
        }
        return editor.commit()
    }

    private data class WidgetRestorePlan(
        val pending: List<JSONObject>,
        val bound: List<Pair<WidgetReference, JSONObject>>
    )

    private fun parseWidgetReferences(raw: String?): List<WidgetReference> {
        if (raw == null) return emptyList()
        val widgets = JSONArray(raw)
        return buildList {
            for (index in 0 until widgets.length()) {
                val item = widgets.getJSONObject(index)
                val id = item.optString("id")
                val appWidgetId = item.optInt("appWidgetId", -1)
                if (id.isNotBlank() && appWidgetId >= 0) {
                    add(WidgetReference(id, appWidgetId, item.optString("provider", "")))
                }
            }
        }
    }

    private fun validateWidgetOptions(saved: JSONArray?) {
        if (saved == null) return
        for (index in 0 until saved.length()) {
            val item = saved.getJSONObject(index)
            item.opt("widgetId")?.let { require(it is String) { "Invalid widget ID" } }
            item.opt("appWidgetId")?.let { require(it is Number) { "Invalid widget system ID" } }
            item.opt("provider")?.let { require(it is String) { "Invalid widget provider" } }
            val values = item.opt("values") ?: continue
            require(values is JSONObject) { "Invalid widget values" }
            values.keys().forEach { key ->
                val value = values.get(key)
                require(value is String || value is Number || value is Boolean) {
                    "Invalid widget option: $key"
                }
                if (key == "launcherNoteCustomized") {
                    require(value is Boolean) { "Invalid note customization flag" }
                }
                if (key == "launcherNoteText") {
                    require(value is String) { "Invalid note text" }
                }
            }
        }
    }

    private fun planWidgetRestore(
        context: Context,
        references: List<WidgetReference>,
        saved: JSONArray?
    ): WidgetRestorePlan {
        val oldIdToWidgetId = references.associate { it.appWidgetId to it.id }
        val savedByWidgetId = mutableMapOf<String, JSONObject>()
        if (saved != null) {
            for (index in 0 until saved.length()) {
                val item = saved.getJSONObject(index)
                val widgetId = item.optString("widgetId").takeIf { it.isNotBlank() }
                    ?: oldIdToWidgetId[item.optInt("appWidgetId", -1)]
                    ?: continue
                savedByWidgetId[widgetId] = item
            }
        }
        val manager = AppWidgetManager.getInstance(context)
        val pending = mutableListOf<JSONObject>()
        val bound = mutableListOf<Pair<WidgetReference, JSONObject>>()
        references.forEach { widget ->
            val item = savedByWidgetId[widget.id]
            val values = item?.optJSONObject("values") ?: JSONObject()
            val expectedProvider = item?.optString("provider")?.takeIf { it.isNotBlank() }
                ?: widget.provider
            val info = runCatching { manager.getAppWidgetInfo(widget.appWidgetId) }.getOrNull()
            val bindingValid = info != null && (
                expectedProvider.isBlank() || info.provider.flattenToString() == expectedProvider
            )
            if (bindingValid) {
                bound += widget to values
            } else {
                pending += JSONObject()
                    .put("widgetId", widget.id)
                    .put("provider", expectedProvider)
                    .put("sourceAppWidgetId", item?.optInt("appWidgetId", widget.appWidgetId) ?: widget.appWidgetId)
                    .put("values", values)
                    .put("autoAttempted", false)
            }
        }
        return WidgetRestorePlan(pending, bound)
    }

    private fun applyWidgetValues(context: Context, widget: WidgetReference, values: JSONObject) {
        if (widget.provider.contains("LauncherNoteWidgetProvider") && values.has("launcherNoteText")) {
            LauncherNoteWidgetStore.set(
                context, widget.appWidgetId, values.getString("launcherNoteText"),
                customized = values.optBoolean(
                    "launcherNoteCustomized", LauncherNoteWidgetStore.isCustomized(context, widget.appWidgetId)
                )
            )
        }
        val bundle = jsonToBundle(values)
        if (!bundle.isEmpty) {
            runCatching { AppWidgetManager.getInstance(context).updateAppWidgetOptions(widget.appWidgetId, bundle) }
        }
    }

    fun remapSystemRestoredWidgetIds(context: Context, oldIds: IntArray, newIds: IntArray) {
        if (oldIds.isEmpty() || oldIds.size != newIds.size) return
        val mapping = oldIds.indices.associate { oldIds[it] to newIds[it] }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WIDGETS, null) ?: return
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return
        var changed = false
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val oldId = item.optInt("appWidgetId", -1)
            val newId = mapping[oldId] ?: continue
            if (newId != oldId) {
                if (item.optString("provider").contains("LauncherNoteWidgetProvider")) {
                    LauncherNoteWidgetStore.set(
                        context, newId, LauncherNoteWidgetStore.get(context, oldId),
                        customized = LauncherNoteWidgetStore.isCustomized(context, oldId)
                    )
                    LauncherNoteWidgetStore.remove(context, oldId)
                }
                item.put("appWidgetId", newId)
                changed = true
            }
        }
        if (changed) {
            prefs.edit().putString(KEY_WIDGETS, array.toString()).commit()
        }
    }

    /**
     * IDs which should receive one automatic rebind attempt after a settings
     * restore.  A cancelled/failed attempt remains available through the normal
     * manual "Restore" action, but will not repeatedly open Android dialogs.
     */
    fun pendingAutomaticWidgetRestoreIds(context: Context): Set<String> =
        pendingWidgetRestoreRecords(context)
            .filterNot { it.optBoolean("autoAttempted", false) }
            .mapNotNull { it.optString("widgetId").takeIf { value -> value.isNotBlank() } }
            .toSet()

    fun markWidgetRestoreAutoAttempted(context: Context, workspaceWidgetId: String) {
        updatePendingWidgetRestoreRecords(context) { records ->
            records.forEach { item ->
                if (item.optString("widgetId") == workspaceWidgetId) {
                    item.put("autoAttempted", true)
                }
            }
        }
    }

    /** Apply provider-specific saved options to a freshly allocated appWidgetId. */
    fun applyPendingWidgetOptions(context: Context, workspaceWidgetId: String, appWidgetId: Int) {
        val item = pendingWidgetRestoreRecords(context)
            .firstOrNull { it.optString("widgetId") == workspaceWidgetId }
            ?: return
        val values = item.optJSONObject("values") ?: return
        if (values.has("launcherNoteText") &&
            item.optString("provider").contains("LauncherNoteWidgetProvider")) {
            LauncherNoteWidgetStore.set(
                context, appWidgetId, values.optString("launcherNoteText"),
                customized = values.optBoolean(
                    "launcherNoteCustomized", LauncherNoteWidgetStore.isCustomized(
                        context, item.optInt("sourceAppWidgetId", appWidgetId)
                    )
                )
            )
        }
        val bundle = jsonToBundle(values)
        if (!bundle.isEmpty) {
            runCatching {
                AppWidgetManager.getInstance(context).updateAppWidgetOptions(appWidgetId, bundle)
            }
        }
    }

    fun completePendingWidgetRestore(context: Context, workspaceWidgetId: String) {
        updatePendingWidgetRestoreRecords(context) { records ->
            records.removeAll { it.optString("widgetId") == workspaceWidgetId }
        }
    }

    private fun widgetReferences(context: Context): List<WidgetReference> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WIDGETS, null) ?: return emptyList()
        return runCatching {
            val widgets = JSONArray(raw)
            buildList {
                for (index in 0 until widgets.length()) {
                    val item = widgets.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val appWidgetId = item.optInt("appWidgetId", -1)
                    if (id.isBlank() || appWidgetId < 0) continue
                    add(
                        WidgetReference(
                            id = id,
                            appWidgetId = appWidgetId,
                            provider = item.optString("provider", "")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    @Suppress("DEPRECATION")
    private fun collectWidgetOptions(context: Context): JSONArray {
        val manager = AppWidgetManager.getInstance(context)
        return JSONArray().apply {
            widgetReferences(context).forEach { widget ->
                val encoded = JSONObject()
                val options = runCatching { manager.getAppWidgetOptions(widget.appWidgetId) }
                    .getOrNull()
                options?.keySet()?.forEach { key ->
                    when (val value = options.get(key)) {
                        is String, is Int, is Long, is Double, is Boolean -> encoded.put(key, value)
                        is Float -> encoded.put(key, value.toDouble())
                    }
                }
                if (widget.provider.contains("LauncherNoteWidgetProvider")) {
                    encoded.put("launcherNoteText", LauncherNoteWidgetStore.get(context, widget.appWidgetId))
                    encoded.put("launcherNoteCustomized", LauncherNoteWidgetStore.isCustomized(context, widget.appWidgetId))
                }
                put(
                    JSONObject()
                        .put("widgetId", widget.id)
                        .put("appWidgetId", widget.appWidgetId)
                        .put("provider", widget.provider)
                        .put("values", encoded)
                )
            }
        }
    }

    private fun jsonToBundle(values: JSONObject): Bundle = Bundle().apply {
        values.keys().forEach { key ->
            when (val value = values.get(key)) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
            }
        }
    }

    private fun pendingWidgetRestoreRecords(context: Context): MutableList<JSONObject> {
        val raw = context.getSharedPreferences(WIDGET_RESTORE_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_WIDGET_RESTORE, null) ?: return mutableListOf()
        return runCatching {
            val array = JSONArray(raw)
            MutableList(array.length()) { index -> array.getJSONObject(index) }
        }.getOrDefault(mutableListOf())
    }

    private fun savePendingWidgetRestoreRecords(context: Context, records: List<JSONObject>): Boolean {
        val prefs = context.getSharedPreferences(WIDGET_RESTORE_PREFS_NAME, Context.MODE_PRIVATE)
        if (records.isEmpty()) {
            return prefs.edit().remove(KEY_PENDING_WIDGET_RESTORE).commit()
        } else {
            return prefs.edit()
                .putString(KEY_PENDING_WIDGET_RESTORE, JSONArray(records).toString())
                .commit()
        }
    }

    private inline fun updatePendingWidgetRestoreRecords(
        context: Context,
        block: (MutableList<JSONObject>) -> Unit
    ) {
        val records = pendingWidgetRestoreRecords(context)
        block(records)
        savePendingWidgetRestoreRecords(context, records)
    }
}
