package org.collegiumdesideriorum.e_e_launcher

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

internal enum class LauncherLanguage(
    val languageTag: String,
    @param:StringRes override val titleRes: Int
) : LocalizedTitle {
    SYSTEM("", R.string.ui_system_language),
    ENGLISH("en", R.string.language_english),
    RUSSIAN("ru", R.string.language_russian),
    GERMAN("de", R.string.language_german),
    FRENCH("fr", R.string.language_french),
    SPANISH("es", R.string.language_spanish),
    PORTUGUESE_BRAZIL("pt-BR", R.string.language_portuguese_brazil),
    JAPANESE("ja", R.string.language_japanese),
    CHINESE_SIMPLIFIED("zh-CN", R.string.language_chinese_simplified)
}

internal fun currentLauncherLanguage(context: Context): LauncherLanguage {
    val localeManager = context.getSystemService(LocaleManager::class.java)
    val selectedTag = localeManager.applicationLocales.toLanguageTags()
        .substringBefore(',')
        .trim()
    if (selectedTag.isEmpty()) return LauncherLanguage.SYSTEM
    return LauncherLanguage.values().firstOrNull {
        it.languageTag.equals(selectedTag, ignoreCase = true)
    } ?: LauncherLanguage.SYSTEM
}

private fun setLauncherLanguage(context: Context, language: LauncherLanguage) {
    context.getSystemService(LocaleManager::class.java).applicationLocales =
        if (language == LauncherLanguage.SYSTEM) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(language.languageTag)
        }
    // RemoteViews do not automatically re-render when the launcher changes
    // its app-specific locale. Refresh only our own welcome-note widgets; the
    // store preserves any notes that users have customized themselves.
    val effectiveContext = if (language.languageTag.isBlank()) context else {
        context.createConfigurationContext(
            android.content.res.Configuration(context.resources.configuration).apply {
                setLocale(java.util.Locale.forLanguageTag(language.languageTag))
            }
        )
    }
    val manager = android.appwidget.AppWidgetManager.getInstance(effectiveContext)
    manager.getAppWidgetIds(
        android.content.ComponentName(effectiveContext, LauncherNoteWidgetProvider::class.java)
    ).forEach { id -> LauncherNoteWidgetStore.update(effectiveContext, id, manager) }
}

@Composable
internal fun LauncherLanguageSettings() {
    val context = LocalContext.current
    val selected = currentLauncherLanguage(context)
    SettingsSectionTitle(l10n(R.string.ui_language))
    LauncherLanguage.values().forEach { language ->
        SimpleRadioOption(language.title, language == selected) {
            setLauncherLanguage(context, language)
        }
    }
}

internal fun languageSettingsTitle(): String = "🌐 ${l10n(R.string.ui_language)}"
