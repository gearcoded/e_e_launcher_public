package org.collegiumdesideriorum.e_e_launcher

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal data class AdvancedLauncherPreferences(
    val workspaceLocked: Boolean = false,
    val sideDockLocked: Boolean = false,
    val bottomDockLocked: Boolean = false,
    val globalIconPackPackage: String? = null,
    val safeMode: Boolean = false,
    val workspaceOverviewMode: WorkspaceOverviewMode = WorkspaceOverviewMode.EDITING,
    val effectPreviewMode: EffectPreviewMode = EffectPreviewMode.INLINE
)

internal enum class WorkspaceOverviewMode {
    NAVIGATION_ONLY,
    EDITING
}

internal enum class EffectPreviewMode {
    INLINE,
    LIVE_LAUNCHER
}

internal object AdvancedLauncherStorage {
    private const val NAME = "e_e_launcher_advanced"
    private const val WELCOME_DONE = "welcome_done"
    private const val WORKSPACE_LOCKED = "workspace_locked"
    // Old setting used by 0.37.26 and earlier: apply to BOTH docks on upgrade/old JSON restore.
    private const val LEGACY_DOCK_LOCKED = "dock_locked"
    private const val SIDE_DOCK_LOCKED = "side_dock_locked"
    private const val BOTTOM_DOCK_LOCKED = "bottom_dock_locked"
    private const val GLOBAL_ICON_PACK = "global_icon_pack"
    private const val SAFE_MODE = "safe_mode"
    private const val UNCAUGHT_CRASH_DETECTED = "uncaught_crash_detected"
    private const val AUTOMATIC_SAFE_MODE = "automatic_safe_mode"
    private const val RECOVERY_POLICY_VERSION = "recovery_policy_version"
    private const val LAST_PROCESSED_EXIT_TIMESTAMP = "last_processed_exit_timestamp"
    private const val CURRENT_RECOVERY_POLICY_VERSION = 2
    private const val WORKSPACE_OVERVIEW_MODE = "workspace_overview_mode"
    private const val EFFECT_PREVIEW_MODE = "effect_preview_mode"
    @Volatile private var crashHandlerInstalled = false
    @Volatile private var startupInitialized = false

    private fun prefs(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isWelcomeDone(context: Context): Boolean = prefs(context).getBoolean(WELCOME_DONE, false)
    fun finishWelcome(context: Context) { prefs(context).edit().putBoolean(WELCOME_DONE, true).apply() }

    /** Records uncaught Java/Kotlin failures immediately. ApplicationExitInfo is
     * also consulted on the next process start so native crashes are covered.
     */
    @Synchronized
    fun installCrashHandler(context: Context) {
        if (crashHandlerInstalled) return
        val applicationContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                prefs(applicationContext).edit()
                    .putBoolean(UNCAUGHT_CRASH_DETECTED, true)
                    .commit()
                LauncherDiagnostics.error(
                    "uncaught crash thread=${thread.name} type=${error.javaClass.name}",
                    error
                )
            }
            previous?.uncaughtException(thread, error)
        }
        crashHandlerInstalled = true
    }

    private fun latestProcessExit(context: Context): ApplicationExitInfo? {
        val activityManager = context.getSystemService(ActivityManager::class.java) ?: return null
        return runCatching {
            activityManager.getHistoricalProcessExitReasons(context.packageName, 0, 1).firstOrNull()
        }.onFailure {
            LauncherDiagnostics.error("process exit history query failed", it)
        }.getOrNull()
    }

    private fun isCrashExit(reason: Int): Boolean =
        reason == ApplicationExitInfo.REASON_CRASH ||
            reason == ApplicationExitInfo.REASON_CRASH_NATIVE

    private fun exitReasonName(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_CRASH -> "crash"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "native_crash"
        else -> reason.toString()
    }

    /**
     * Enters automatic safe mode after a real launcher crash until one launch
     * survives the startup health check.
     * The uncaught-exception marker covers Java/Kotlin crashes, while
     * ApplicationExitInfo also catches native process crashes. Ordinary Android
     * process termination is deliberately ignored.
     *
     * Recovery-policy v2 establishes an ApplicationExitInfo baseline on its first
     * launch so a stale historical crash from an older launcher version cannot
     * unexpectedly enable safe mode after an update.
     */
    @Synchronized
    fun beginStartup(context: Context): Boolean {
        if (startupInitialized) return false
        val preferences = prefs(context)
        val previousPolicyVersion = preferences.getInt(RECOVERY_POLICY_VERSION, 0)
        val migratingFromPendingStartupRecovery = previousPolicyVersion < 1
        val initializingExitHistory = previousPolicyVersion < CURRENT_RECOVERY_POLICY_VERSION
        val lastProcessedExitTimestamp = preferences.getLong(LAST_PROCESSED_EXIT_TIMESTAMP, 0L)
        val latestExit = latestProcessExit(context)
        val newHistoricalExit = latestExit?.takeIf {
            !initializingExitHistory && it.timestamp > lastProcessedExitTimestamp
        }
        val recoveringFromUncaughtCrash =
            preferences.getBoolean(UNCAUGHT_CRASH_DETECTED, false)
        val recoveringFromHistoricalCrash =
            newHistoricalExit?.let { isCrashExit(it.reason) } == true
        val recoveringFromCrash =
            recoveringFromUncaughtCrash || recoveringFromHistoricalCrash
        val safeModeAlreadyEnabled = preferences.getBoolean(SAFE_MODE, false)
        val automaticSafeModeAlready = preferences.getBoolean(AUTOMATIC_SAFE_MODE, false)

        val editor = preferences.edit()
            .putInt(RECOVERY_POLICY_VERSION, CURRENT_RECOVERY_POLICY_VERSION)
            .remove(UNCAUGHT_CRASH_DETECTED)
            // Clean up keys used by the pre-0.35.14 pending-start heuristic.
            .remove("startup_pending")
            .remove("startup_process_id")

        if (initializingExitHistory) {
            // Ignore all exit records that predate this recovery-policy upgrade.
            // A native crash occurring later in this very process will have a
            // newer timestamp and will therefore be detected on the next start.
            editor.putLong(
                LAST_PROCESSED_EXIT_TIMESTAMP,
                maxOf(System.currentTimeMillis(), latestExit?.timestamp ?: 0L)
            )
        } else if (latestExit != null && latestExit.timestamp > lastProcessedExitTimestamp) {
            editor.putLong(LAST_PROCESSED_EXIT_TIMESTAMP, latestExit.timestamp)
        }

        val enterAutomaticRecovery = when {
            recoveringFromCrash && (!safeModeAlreadyEnabled || automaticSafeModeAlready) -> {
                editor
                    .putBoolean(SAFE_MODE, true)
                    .putBoolean(AUTOMATIC_SAFE_MODE, true)
                true
            }
            recoveringFromCrash -> {
                // Safe Mode was already selected manually. Keep it persistent
                // rather than converting it into an automatically cleared mode.
                editor.remove(AUTOMATIC_SAFE_MODE)
                false
            }
            automaticSafeModeAlready -> {
                // A previous recovery attempt ended before the four-second health
                // check. Stay in automatic Safe Mode until one launch is healthy.
                editor.putBoolean(SAFE_MODE, true)
                true
            }
            else -> {
                editor.remove(AUTOMATIC_SAFE_MODE)
                false
            }
        }

        // Very old versions could not distinguish a normal process stop from a
        // crash and could leave recovery mode persistent. This migration takes
        // precedence over any legacy automatic marker.
        if (migratingFromPendingStartupRecovery) {
            editor
                .putBoolean(SAFE_MODE, false)
                .remove(AUTOMATIC_SAFE_MODE)
        }

        editor.commit()
        startupInitialized = true

        LauncherDiagnostics.info(
            "startup crashFlag=$recoveringFromUncaughtCrash " +
                "exit=${newHistoricalExit?.let { exitReasonName(it.reason) } ?: "none"} " +
                "exitTimestamp=${newHistoricalExit?.timestamp ?: 0L} " +
                "exitBaseline=$initializingExitHistory " +
                "policyMigration=$migratingFromPendingStartupRecovery " +
                "storedSafe=${preferences.getBoolean(SAFE_MODE, false)} " +
                "automaticRecovery=$enterAutomaticRecovery"
        )
        return enterAutomaticRecovery
    }

    fun markStartupSuccessful(context: Context) {
        val preferences = prefs(context)
        val automaticSafeMode = preferences.getBoolean(AUTOMATIC_SAFE_MODE, false)
        preferences.edit()
            .remove(UNCAUGHT_CRASH_DETECTED)
            .apply {
                // Automatic crash recovery is one launch only. A safe mode
                // selected manually in settings or on the welcome screen persists.
                if (automaticSafeMode) {
                    putBoolean(SAFE_MODE, false)
                    remove(AUTOMATIC_SAFE_MODE)
                }
            }
            .commit()
        LauncherDiagnostics.info("startup healthy automaticSafeCleared=$automaticSafeMode")
    }

    fun load(context: Context) = AdvancedLauncherPreferences(
        workspaceLocked = prefs(context).getBoolean(WORKSPACE_LOCKED, false),
        sideDockLocked = prefs(context).getBoolean(
            SIDE_DOCK_LOCKED, prefs(context).getBoolean(LEGACY_DOCK_LOCKED, false)
        ),
        bottomDockLocked = prefs(context).getBoolean(
            BOTTOM_DOCK_LOCKED, prefs(context).getBoolean(LEGACY_DOCK_LOCKED, false)
        ),
        globalIconPackPackage = prefs(context).getString(GLOBAL_ICON_PACK, null),
        safeMode = prefs(context).getBoolean(SAFE_MODE, false),
        workspaceOverviewMode = runCatching {
            WorkspaceOverviewMode.valueOf(
                prefs(context).getString(
                    WORKSPACE_OVERVIEW_MODE,
                    WorkspaceOverviewMode.EDITING.name
                ) ?: WorkspaceOverviewMode.EDITING.name
            )
        }.getOrDefault(WorkspaceOverviewMode.EDITING),
        effectPreviewMode = runCatching {
            EffectPreviewMode.valueOf(
                prefs(context).getString(EFFECT_PREVIEW_MODE, EffectPreviewMode.INLINE.name)
                    ?: EffectPreviewMode.INLINE.name
            )
        }.getOrDefault(EffectPreviewMode.INLINE)
    )

    fun save(context: Context, value: AdvancedLauncherPreferences) {
        prefs(context).edit()
            .putBoolean(WORKSPACE_LOCKED, value.workspaceLocked)
            .putBoolean(SIDE_DOCK_LOCKED, value.sideDockLocked)
            .putBoolean(BOTTOM_DOCK_LOCKED, value.bottomDockLocked)
            .remove(LEGACY_DOCK_LOCKED)
            .putBoolean(SAFE_MODE, value.safeMode)
            .putString(WORKSPACE_OVERVIEW_MODE, value.workspaceOverviewMode.name)
            .putString(EFFECT_PREVIEW_MODE, value.effectPreviewMode.name)
            .remove(AUTOMATIC_SAFE_MODE)
            .apply {
                if (value.globalIconPackPackage == null) remove(GLOBAL_ICON_PACK)
                else putString(GLOBAL_ICON_PACK, value.globalIconPackPackage)
            }
            .apply()
    }
}

internal fun ComponentActivity.requestHomeRole(launcher: ActivityResultLauncher<Intent>) {
    val roleManager = getSystemService(RoleManager::class.java) ?: return
    if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME) || roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return
    launcher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
}

@Composable
internal fun WelcomeScreen(
    onContinue: () -> Unit,
    onSafeMode: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(l10n(R.string.ui_welcome_to_launcher), style = MaterialTheme.typography.headlineMedium)
            Text(
                l10n(R.string.ui_welcome_home_description),
                modifier = Modifier.padding(top = 14.dp, bottom = 28.dp)
            )
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text(l10n(R.string.ui_continue)) }
            OutlinedButton(onClick = onSafeMode, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(l10n(R.string.ui_start_in_safe_mode))
            }
            TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(l10n(R.string.ui_skip)) }
        }
    }
}

/** Settings controls shared between the Security page and the legacy dialog. */
@Composable
internal fun AdvancedFeaturesSettings(
    value: AdvancedLauncherPreferences,
    onValueChange: (AdvancedLauncherPreferences) -> Unit,
    onRequestHomeRole: () -> Unit
) {
    var packs by remember { mutableStateOf<List<IconPackDescriptor>?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        packs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            IconPackRepository.discover(context.applicationContext)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onRequestHomeRole, modifier = Modifier.fillMaxWidth()) {
            Text(l10n(R.string.ui_set_as_home_app))
        }
        Toggle(l10n(R.string.ui_lock_desktop_editing), value.workspaceLocked) {
            onValueChange(value.copy(workspaceLocked = it))
        }
        Toggle(l10n(R.string.ui_lock_side_dock_changes), value.sideDockLocked) {
            onValueChange(value.copy(sideDockLocked = it))
        }
        Toggle(l10n(R.string.ui_lock_bottom_dock_changes), value.bottomDockLocked) {
            onValueChange(value.copy(bottomDockLocked = it))
        }
        Toggle(l10n(R.string.ui_safe_mode), value.safeMode) {
            onValueChange(value.copy(safeMode = it))
        }
        Text(l10n(R.string.ui_icon_pack), style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { onValueChange(value.copy(globalIconPackPackage = null)) }) {
            Text(l10n(R.string.ui_system_icons))
        }
        packs.orEmpty().forEach { pack ->
            TextButton(onClick = { onValueChange(value.copy(globalIconPackPackage = pack.packageName)) }) {
                Text((if (value.globalIconPackPackage == pack.packageName) "✓ " else "") + pack.label)
            }
        }
    }
}

@Composable
internal fun AdvancedFeaturesDialog(
    initial: AdvancedLauncherPreferences,
    onDismiss: () -> Unit,
    onRequestHomeRole: () -> Unit,
    onSave: (AdvancedLauncherPreferences) -> Unit
) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(l10n(R.string.ui_launcher_protection_and_compatibility)) },
        text = {
            AdvancedFeaturesSettings(
                value = value,
                onValueChange = { value = it },
                onRequestHomeRole = onRequestHomeRole
            )
        },
        confirmButton = { TextButton(onClick = { onSave(value) }) { Text(l10n(R.string.ui_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(l10n(R.string.ui_cancel)) } }
    )
}

@Composable
private fun Toggle(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
