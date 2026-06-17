package com.xposed.spaceswitcher

import android.content.Context
import android.os.UserHandle
import de.robv.android.xposed.XposedBridge

object SpaceDetector {

    fun getCurrentUserId(): Int = UserHandle.myUserId()

    fun isMainSpace(): Boolean = getCurrentUserId() == 0

    fun isSecondarySpace(): Boolean = getCurrentUserId() != 0

    fun getSecondSpaceId(context: Context): Int {
        return try {
            val clazz = Class.forName("com.miui.securitycore.config.SecurityCoreConfig")
            val method = clazz.getDeclaredMethod("getSecondSpaceId", Context::class.java)
            (method.invoke(null, context) as? Int) ?: 10
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Failed to get second space ID: ${e.message}")
            10
        }
    }

    fun getAllSpaces(context: Context): List<Int> {
        val spaces = mutableListOf<Int>()
        try {
            val userManager = context.getSystemService("user") as? android.os.UserManager
                ?: return spaces

            val getUsersMethod = userManager::class.java.getDeclaredMethod("getUsers")
            val users = getUsersMethod.invoke(userManager) as? List<*> ?: return spaces

            users.forEach { user ->
                try {
                    val idField = user::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    val userId = idField.get(user) as Int
                    spaces.add(userId)
                } catch (e: Exception) {
                    XposedBridge.log("[SpaceSwitch] Failed to parse user: ${e.message}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Failed to get all spaces: ${e.message}")
        }
        return spaces.sorted()
    }
}
