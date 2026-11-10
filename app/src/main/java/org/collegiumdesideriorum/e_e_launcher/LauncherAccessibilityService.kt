package org.collegiumdesideriorum.e_e_launcher

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class LauncherAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeService = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (activeService === this) activeService = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (activeService === this) activeService = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var activeService: LauncherAccessibilityService? = null

        fun openNotificationShade(): Boolean = activeService?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
        ) == true

        fun lockScreen(): Boolean = activeService?.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
        ) == true
    }
}
