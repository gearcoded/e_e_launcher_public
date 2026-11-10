package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject


internal val SUPPORTED_WIDGET_INTENT_ACTIONS: Set<String> = setOf(
    Settings.ACTION_WIFI_SETTINGS,
    Settings.ACTION_BLUETOOTH_SETTINGS,
    Settings.ACTION_DISPLAY_SETTINGS,
    Settings.ACTION_SOUND_SETTINGS,
    Settings.ACTION_APPLICATION_SETTINGS,
    Settings.ACTION_ACCESSIBILITY_SETTINGS,
    Settings.ACTION_BATTERY_SAVER_SETTINGS,
    Settings.ACTION_SETTINGS
)

internal fun isSupportedWidgetIntentAction(action: String): Boolean =
    action in SUPPORTED_WIDGET_INTENT_ACTIONS

internal enum class WidgetActionRegionMode {
    FULL,
    CUSTOM
}

internal data class WidgetActionRegion(
    val leftPercent: Int = 0,
    val topPercent: Int = 0,
    val widthPercent: Int = 100,
    val heightPercent: Int = 100
) {
    fun normalized(): WidgetActionRegion {
        val left = leftPercent.coerceIn(0, 95)
        val top = topPercent.coerceIn(0, 95)
        val width = widthPercent.coerceIn(5, 100 - left)
        val height = heightPercent.coerceIn(5, 100 - top)
        return WidgetActionRegion(left, top, width, height)
    }
}

internal enum class WidgetActionStepType {
    APP,
    LAUNCHER_ACTION,
    INTENT_ACTION
}

internal data class WidgetActionStep(
    val type: WidgetActionStepType,
    val value: String,
    val label: String
)

internal data class WidgetActionChain(
    val id: String,
    val name: String,
    val widgetIds: Set<String>,
    val regionMode: WidgetActionRegionMode = WidgetActionRegionMode.FULL,
    val region: WidgetActionRegion = WidgetActionRegion(),
    val actions: List<WidgetActionStep> = emptyList()
)

internal data class WidgetActionWidgetDescriptor(
    val id: String,
    val title: String,
    val subtitle: String
)

internal object WidgetActionChainStorage {
    private const val PREFS_NAME = "e_e_launcher_workspace"
    private const val KEY = "widget_action_chains"

    fun load(context: Context): List<WidgetActionChain> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                    val widgetIds = item.optJSONArray("widgetIds")?.let { ids ->
                        buildSet {
                            for (i in 0 until ids.length()) {
                                ids.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                            }
                        }
                    }.orEmpty()
                    val regionMode = runCatching {
                        WidgetActionRegionMode.valueOf(item.optString("regionMode", WidgetActionRegionMode.FULL.name))
                    }.getOrDefault(WidgetActionRegionMode.FULL)
                    val regionJson = item.optJSONObject("region")
                    val region = WidgetActionRegion(
                        leftPercent = regionJson?.optInt("left", 0) ?: 0,
                        topPercent = regionJson?.optInt("top", 0) ?: 0,
                        widthPercent = regionJson?.optInt("width", 100) ?: 100,
                        heightPercent = regionJson?.optInt("height", 100) ?: 100
                    ).normalized()
                    val actions = item.optJSONArray("actions")?.let { actionArray ->
                        buildList {
                            for (actionIndex in 0 until actionArray.length()) {
                                val action = actionArray.optJSONObject(actionIndex) ?: continue
                                val type = runCatching {
                                    WidgetActionStepType.valueOf(action.optString("type"))
                                }.getOrNull() ?: continue
                                val value = action.optString("value").takeIf { it.isNotBlank() } ?: continue
                                // Imported/manual JSON is untrusted input.  Only the
                                // explicit system-settings actions offered by the UI are
                                // allowed to become executable INTENT_ACTION steps.
                                if (type == WidgetActionStepType.INTENT_ACTION &&
                                    !isSupportedWidgetIntentAction(value)
                                ) continue
                                add(
                                    WidgetActionStep(
                                        type = type,
                                        value = value,
                                        label = action.optString("label", value).ifBlank { value }
                                    )
                                )
                            }
                        }
                    }.orEmpty()
                    add(
                        WidgetActionChain(
                            id = id,
                            name = item.optString("name", "").ifBlank { "Widget action chain" },
                            widgetIds = widgetIds,
                            regionMode = regionMode,
                            region = region,
                            actions = actions
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, chains: List<WidgetActionChain>) {
        // A widget can belong to only one chain.  Keep the last edited chain's
        // ownership when malformed/legacy data contains duplicates.
        val claimed = mutableSetOf<String>()
        val normalized = chains.asReversed().map { chain ->
            val owned = chain.widgetIds.filterNot { it in claimed }.toSet()
            claimed += owned
            chain.copy(widgetIds = owned, region = chain.region.normalized())
        }.asReversed()

        val array = JSONArray()
        normalized.forEach { chain ->
            array.put(JSONObject().apply {
                put("id", chain.id)
                put("name", chain.name)
                put("widgetIds", JSONArray(chain.widgetIds.toList()))
                put("regionMode", chain.regionMode.name)
                put("region", JSONObject().apply {
                    val region = chain.region.normalized()
                    put("left", region.leftPercent)
                    put("top", region.topPercent)
                    put("width", region.widthPercent)
                    put("height", region.heightPercent)
                })
                put("actions", JSONArray().apply {
                    chain.actions
                        .filterNot { action ->
                            action.type == WidgetActionStepType.INTENT_ACTION &&
                                !isSupportedWidgetIntentAction(action.value)
                        }
                        .forEach { action ->
                            put(JSONObject().apply {
                                put("type", action.type.name)
                                put("value", action.value)
                                put("label", action.label)
                            })
                        }
                })
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY, array.toString()).apply()
    }
}
