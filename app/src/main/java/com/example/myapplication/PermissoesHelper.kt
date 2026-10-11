package com.example.myapplication

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object PermissoesHelper {

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false

            val expected = ComponentName(context, SentinelNotificationListener::class.java).flattenToString()
            enabledListeners.split(":").any { it == expected }
        } catch (_: Throwable) {
            false
        }
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isCallScreeningRoleHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return false
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } catch (_: Throwable) {
            false
        }
    }
}