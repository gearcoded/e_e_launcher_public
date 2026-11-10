package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import android.app.LocaleManager
import android.os.LocaleList

/** Reset only the requested domain. User-created files and Android system wallpaper are not touched. */
internal enum class LauncherResetArea {
    WORKSPACE, APP_DRAWER, SIDE_DOCK, BOTTOM_DOCK, INTERFACE, ALL
}

internal object LauncherReset {
    private const val WORKSPACE_PREFS = "e_e_launcher_workspace"
    private const val ADVANCED_PREFS = "e_e_launcher_advanced"
    private const val WIDGET_RESTORE_PREFS = "e_e_launcher_widget_restore"

    private val workspaceKeys = setOf(
        "page_count", "shortcuts", "widgets", "folders", "workspace_notes", "default_workspace_page",
        "page_transition", "mesh_snapshot_after_idle", "workspace_min_effect_ms", "workspace_loop",
        "widget_action_chains", "widget_edit_bar_position", "ignore_gestures_on_widgets",
        "keep_uninstalled_app_shortcuts",
        "swipe_down_action", "swipe_up_action", "double_tap_action", "pinch_action"
    )
    private val drawerKeys = setOf("app_sort_mode", "hidden_apps", "opened_apps", "app_customizations", "drawer_min_effect_ms")

    /** Explicit domain boundaries protect unrelated settings against accidental deletion. */
    internal fun belongsTo(area: LauncherResetArea, key: String): Boolean = when (area) {
        LauncherResetArea.ALL -> true
        LauncherResetArea.WORKSPACE -> key in workspaceKeys ||
            key.startsWith("effect_group_expanded_") || key.startsWith("multi_swipe_") ||
            key.startsWith("launcher_note_widget_text_")
        LauncherResetArea.APP_DRAWER -> key.startsWith("app_drawer_") ||
            key.startsWith("new_app_") || key in drawerKeys
        LauncherResetArea.SIDE_DOCK -> key.startsWith("side_dock_") &&
            key != "side_dock_gesture_migrated_v11"
        LauncherResetArea.BOTTOM_DOCK -> key.startsWith("bottom_dock_")
        LauncherResetArea.INTERFACE -> key == "launcher_interface_style" ||
            key == "launcher_dialog_style" || key.startsWith("text_shadow_") ||
            key.startsWith("wallpaper_") || key == "environment_effects" ||
            key.startsWith("folder_fan_") || key.startsWith("folder_reorder_") ||
            key.startsWith("app_launch_") || key.startsWith("app_drag_") ||
            key == "link_launch_delay_to_animation" || key == "widget_picker_mode"
    }

    /** Empty the workspace without resetting page count, gesture mappings or effects. */
    fun clearWorkspaceItems(context: Context): Boolean {
        val prefs = context.getSharedPreferences(WORKSPACE_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .remove("shortcuts")
            .remove("widgets")
            .remove("folders")
            .remove("workspace_notes")
            .remove("widget_action_chains")
        prefs.all.keys.filter { it.startsWith("launcher_note_widget_text_") }
            .forEach { key -> editor.remove(key) }
        if (!editor.commit()) return false
        return context.getSharedPreferences(WIDGET_RESTORE_PREFS, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    fun apply(context: Context, area: LauncherResetArea): Boolean {
        val prefs = context.getSharedPreferences(WORKSPACE_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        if (area == LauncherResetArea.ALL) editor.clear()
        else prefs.all.keys.filter { belongsTo(area, it) }.forEach { editor.remove(it) }
        if (!editor.commit()) return false

        val advanced = context.getSharedPreferences(ADVANCED_PREFS, Context.MODE_PRIVATE)
        val advancedEditor = advanced.edit()
        when (area) {
            LauncherResetArea.ALL -> advancedEditor.clear()
            LauncherResetArea.WORKSPACE -> advancedEditor.remove("workspace_locked")
            LauncherResetArea.SIDE_DOCK -> advancedEditor.putBoolean("side_dock_locked", false)
            LauncherResetArea.BOTTOM_DOCK -> advancedEditor.putBoolean("bottom_dock_locked", false)
            LauncherResetArea.INTERFACE -> advancedEditor.remove("global_icon_pack")
            LauncherResetArea.APP_DRAWER -> Unit
        }
        if (!advancedEditor.commit()) return false

        if (area == LauncherResetArea.WORKSPACE || area == LauncherResetArea.ALL) {
            if (!context.getSharedPreferences(WIDGET_RESTORE_PREFS, Context.MODE_PRIVATE)
                    .edit().clear().commit()) return false
        }
        if (area == LauncherResetArea.ALL) {
            // Language selection is managed by Android, not SharedPreferences.
            runCatching {
                context.getSystemService(LocaleManager::class.java)
                    ?.applicationLocales = LocaleList.getEmptyLocaleList()
            }
        }
        return true
    }
}
