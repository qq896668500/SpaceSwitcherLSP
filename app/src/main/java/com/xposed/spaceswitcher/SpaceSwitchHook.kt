package com.xposed.spaceswitcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object SpaceSwitchHook {

    fun hookSecuritySpace(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.securityspace.ui.activity.SwitchUserActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java
            ) { param ->
                try {
                    val activity = param.thisObject as Activity
                    val bundle = param.args[0] as? Bundle
                    param.invokeOriginalMethod()
                    val targetUserId = bundle?.getInt("params_target_user_id", -10000) ?: -10000
                    val currentUserId = SpaceDetector.getCurrentUserId()
                    if (currentUserId != 0 && targetUserId == 0) {
                        addReturnButton(activity)
                    }
                } catch (e: Exception) {
                    XposedBridge.log("[SpaceSwitch] Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Hook failed: ${e.message}")
        }
    }

    private fun addReturnButton(activity: Activity) {
        try {
            val button = Button(activity).apply {
                text = "返回主空间"
                textSize = 16f
                setOnClickListener {
                    val intent = Intent().apply {
                        setClassName(
                            "com.miui.securityspace",
                            "com.miui.securityspace.ui.activity.SwitchUserActivity"
                        )
                        putExtra("params_target_user_id", 0)
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
            val rootView = activity.window.decorView.findViewById<LinearLayout>(
                android.R.id.content
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rootView?.addView(button, 0, params)
            Toast.makeText(activity, "✓ 返回按钮已添加", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Button error: ${e.message}")
        }
    }
}
