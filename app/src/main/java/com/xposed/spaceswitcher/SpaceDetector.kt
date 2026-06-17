package com.xposed.spaceswitcher

import android.content.Context
import android.os.UserHandle
import de.robv.android.xposed.XposedBridge

object SpaceDetector {

    fun getCurrentUserId(): Int {
        return try {
            UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
        } catch (e: Exception) {
            0
        }
    }

    fun isMainSpace(): Boolean = getCurrentUserId() == 0

    fun isSecondarySpace(): Boolean = getCurrentUserId() != 0

    fun getSecondSpaceId(context: Context): Int {
        return try {
            val clazz = Class.forName("com.miui.securitycore.config.SecurityCoreConfig")
            val method = clazz.getDeclaredMethod("getSecondSpaceId", Context::class.java)
            (method.invoke(null, context) as? Int) ?: 10
        } catch (e: Exception) {
            10
        }
    }
}
