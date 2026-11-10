package org.collegiumdesideriorum.e_e_launcher

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Android backup/restore may preserve AppWidgetHost state while assigning new
 * appWidgetIds.  Translate the launcher's persisted IDs before the workspace is
 * loaded.  The system broadcast is protected and supplies parallel old/new arrays.
 */
class WidgetHostRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AppWidgetManager.ACTION_APPWIDGET_HOST_RESTORED) return
        val hostId = intent.getIntExtra(AppWidgetManager.EXTRA_HOST_ID, -1)
        if (hostId != MainActivity.APP_WIDGET_HOST_ID) return
        val oldIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_OLD_IDS) ?: return
        val newIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return
        LauncherSettingsBackup.remapSystemRestoredWidgetIds(
            context.applicationContext,
            oldIds,
            newIds
        )
    }
}
