package org.collegiumdesideriorum.e_e_launcher

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.InputFilter
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.RemoteViews

/** A regular Android widget. Movement, resize and deletion use WorkspaceWidgetItem unchanged. */
class LauncherNoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { LauncherNoteWidgetStore.update(context, it, manager) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, manager, appWidgetId, newOptions)
        LauncherNoteWidgetStore.update(context, appWidgetId, manager)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { LauncherNoteWidgetStore.remove(context, it) }
        super.onDeleted(context, appWidgetIds)
    }
}

/** Widget text is in launcher workspace preferences, included in JSON backups. */
internal object LauncherNoteWidgetStore {
    private const val PREFS = "e_e_launcher_workspace"
    private const val PREFIX = "launcher_note_widget_text_"

    private val welcomeTextResources = intArrayOf(
        R.string.ui_note_welcome_home, R.string.ui_note_welcome_next
    )
    private const val CUSTOM_PREFIX = "launcher_note_widget_customized_"

    fun get(context: Context, id: Int): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val value = prefs.getString("$PREFIX$id", "").orEmpty()
        if (prefs.getBoolean("$CUSTOM_PREFIX$id", false)) return value
        for (resource in welcomeTextResources) {
            // Recognize both freshly seeded and older welcome notes in any
            // supported language. Once user edits, the customized flag wins.
            val translations = listOf("", "de", "es", "fr", "ja", "pt-rBR", "ru", "zh-rCN")
            for (tag in translations) {
                val locale = if (tag.isEmpty()) java.util.Locale.ENGLISH
                    else java.util.Locale.forLanguageTag(tag.replace("-r", "-"))
                val localizedContext = context.createConfigurationContext(
                    android.content.res.Configuration(context.resources.configuration).apply {
                        setLocale(locale)
                    }
                )
                if (value == localizedContext.getString(resource)) {
                    return context.getString(resource)
                }
            }
        }
        return value
    }

    fun isCustomized(context: Context, id: Int): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("$CUSTOM_PREFIX$id", false)

    fun set(context: Context, id: Int, text: String, customized: Boolean = true) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("$PREFIX$id", text.take(4000))
            .putBoolean("$CUSTOM_PREFIX$id", customized).apply()
        update(context, id)
    }

    fun remove(context: Context, id: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove("$PREFIX$id").remove("$CUSTOM_PREFIX$id").apply()
    }

    fun update(context: Context, id: Int, manager: AppWidgetManager = AppWidgetManager.getInstance(context)) {
        if (manager.getAppWidgetInfo(id)?.provider != ComponentName(context, LauncherNoteWidgetProvider::class.java)) return
        val remote = RemoteViews(context.packageName, R.layout.launcher_note_widget)
        remote.setTextViewText(
            R.id.launcher_note_widget_text,
            get(context, id).ifBlank { context.getString(R.string.ui_note_widget_hint) }
        )
        val intent = Intent(context, LauncherNoteWidgetEditActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        val click = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remote.setOnClickPendingIntent(R.id.launcher_note_widget_root, click)
        manager.updateAppWidget(id, remote)
    }


}

/** Tapping widget opens a regular text editor. Widget resizing/dragging is host-managed. */
class LauncherNoteWidgetEditActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(id)?.provider
        if (provider != ComponentName(this, LauncherNoteWidgetProvider::class.java)) {
            finish()
            return
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val editor = EditText(this).apply {
            setText(LauncherNoteWidgetStore.get(this@LauncherNoteWidgetEditActivity, id))
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = Gravity.TOP or Gravity.START
            minLines = 5
            maxLines = 12
            filters = arrayOf(InputFilter.LengthFilter(4000))
            setPadding(24, 16, 24, 16)
            setSelection(text.length)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.ui_note_title)
            .setView(editor)
            .setNegativeButton(R.string.ui_cancel) { _, _ -> finish() }
            .setPositiveButton(R.string.ui_save) { _, _ ->
                LauncherNoteWidgetStore.set(this, id, editor.text.toString())
                finish()
            }
            .create()
        dialog.setOnCancelListener { finish() }
        dialog.setOnDismissListener { if (!isFinishing) finish() }
        dialog.show()
    }
}
