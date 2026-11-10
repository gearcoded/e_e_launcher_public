package org.collegiumdesideriorum.e_e_launcher

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes

/** Provides resource strings to UI models that are not themselves composables. */
internal object LauncherStrings {
    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun get(@StringRes resourceId: Int, vararg arguments: Any?): String {
        val resources = checkNotNull(applicationContext) {
            "Launcher string resources were requested before application initialization."
        }.resources
        return if (arguments.isEmpty()) {
            resources.getString(resourceId)
        } else {
            val formatArguments = Array<Any>(arguments.size) { index ->
                arguments[index] ?: "null"
            }
            resources.getString(resourceId, *formatArguments)
        }
    }
}

internal fun l10n(@StringRes resourceId: Int, vararg arguments: Any?): String =
    LauncherStrings.get(resourceId, *arguments)

/** Resolves enum and model labels on access so an in-process locale change is never stale. */
internal interface LocalizedTitle {
    @get:StringRes
    val titleRes: Int
    val title: String get() = l10n(titleRes)
}

internal interface LocalizedDescription {
    @get:StringRes
    val descriptionRes: Int
    val description: String get() = l10n(descriptionRes)
}

class EELauncherApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        LauncherStrings.initialize(this)
    }

    override fun onCreate() {
        super.onCreate()
        LauncherStrings.initialize(this)
    }
}
