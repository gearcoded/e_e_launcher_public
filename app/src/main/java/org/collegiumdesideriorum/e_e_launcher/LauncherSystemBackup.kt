package org.collegiumdesideriorum.e_e_launcher

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.content.Context
import android.os.ParcelFileDescriptor

/**
 * User-controlled Android system backup policy.
 *
 * android:allowBackup must remain enabled so Android can invoke our BackupAgent.
 * The agent then either delegates to the normal Auto Backup implementation or
 * intentionally writes no files, depending on this preference.
 * Android 12+ direct device transfer is independently excluded by static
 * data_extraction_rules.xml; it cannot safely follow this dynamic switch.
 */
internal object LauncherSystemBackup {
    private const val PREFS_NAME = "e_e_launcher_workspace"
    private const val KEY_ENABLED = "system_backup_enabled"

    // Preserve the behaviour of older launcher versions on upgrade.
    const val DEFAULT_ENABLED = true

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()

    }
}

/** Android Auto Backup gate controlled by [LauncherSystemBackup]. */
class LauncherBackupAgent : BackupAgent() {
    override fun onFullBackup(data: FullBackupDataOutput) {
        if (LauncherSystemBackup.isEnabled(this)) {
            super.onFullBackup(data)
        }
    }

    // android:fullBackupOnly="true" means Android uses file-based Auto Backup.
    // These key/value callbacks are kept as no-ops only because BackupAgent's
    // abstract contract still requires them.
    override fun onBackup(
        oldState: ParcelFileDescriptor,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor
    ) = Unit

    override fun onRestore(
        data: BackupDataInput,
        appVersionCode: Int,
        newState: ParcelFileDescriptor
    ) = Unit
}
