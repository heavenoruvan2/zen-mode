package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {

    companion object {
        var isFocusModeActive = false
        var activeFocusTask = "Deep Focus Session"
        var blockedAppAttemptListener: ((packageName: String) -> Unit)? = null

        val BLOCKED_PACKAGES = setOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.twitter.android",
            "com.snapchat.android",
            "com.discord"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isFocusModeActive) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (BLOCKED_PACKAGES.contains(packageName)) {
                Log.d("FocusAccessibility", "Blocked app detected: $packageName during focus session")
                blockedAppAttemptListener?.invoke(packageName)
                
                // Perform action to return home to enforce focus
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    override fun onInterrupt() {}
}
