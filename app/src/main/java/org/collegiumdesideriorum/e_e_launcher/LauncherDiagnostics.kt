package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import android.os.Build
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Small, logcat-only diagnostic channel for device-specific launcher failures.
 * It deliberately avoids labels, widget contents, coordinates, and other user
 * data. Filter logcat with the EELauncherDiag tag.
 */
internal object LauncherDiagnostics {
    const val TAG = "EELauncherDiag"
    private val deviceLogged = AtomicBoolean(false)

    fun logDeviceOnce(context: Context) {
        if (!deviceLogged.compareAndSet(false, true)) return
        info(
            "process start package=${context.packageName} " +
                "version=${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) " +
                "manufacturer=${Build.MANUFACTURER} model=${Build.MODEL} " +
                "sdk=${Build.VERSION.SDK_INT} release=${Build.VERSION.RELEASE}"
        )
    }

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun warning(message: String, error: Throwable? = null) {
        if (error == null) Log.w(TAG, message) else Log.w(TAG, message, error)
    }

    fun error(message: String, error: Throwable? = null) {
        if (error == null) Log.e(TAG, message) else Log.e(TAG, message, error)
    }
}
