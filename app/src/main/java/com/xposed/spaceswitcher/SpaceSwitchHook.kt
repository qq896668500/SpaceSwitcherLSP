package com.xposed.spaceswitcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object SpaceSwitchHook {

    fun hookSecuritySpace(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook SwitchUserActivity.onCreate()
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
                    val currentUserId = UserHandle.myUserId()

                    XposedBridge.log("[SpaceSwitch] Current: $currentUserId, Target: $targetUserId")

                    // 在分身空间显示"返回主空间"按钮
                    if (currentUserId != 0 && targetUserId == 0) {
                        addReturnButton(activity)
                    }

                    // 在主空间显示分身列表
                    if (currentUserId == 0) {
                        addSpaceListButton(activity)
                    }
                } catch (e: Exception) {
                    XposedBridge.log("[SpaceSwitch] Error in onCreate: ${e.message}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Failed to hook SwitchUserActivity: ${e.message}")
        }

        try {
            // Hook SwitchUserService.onStartCommand()
            XposedHelpers.findAndHookMethod(
                "com.miui.securityspace.service.SwitchUserService",
                lpparam.classLoader,
                "onStartCommand",
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ) { param ->
                try {
                    val intent = param.args[0] as Intent
                    val targetUserId = intent.getIntExtra("params_target_user_id", -10000)
                    XposedBridge.log("[SpaceSwitch] Switching to userId: $targetUserId")
                    param.invokeOriginalMethod()
                } catch (e: Exception) {
                    XposedBridge.log("[SpaceSwitch] Error in onStartCommand: ${e.message}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Failed to hook SwitchUserService: ${e.message}")
        }
    }

    fun hookLauncher(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("[SpaceSwitch] Launcher hook initialized for ${lpparam.packageName}")
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
                        putExtra("com.miui.xspace.preference_from_type", "lsp_module")
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

            Toast.makeText(activity, "✓ 已补完返回按钮", Toast.LENGTH_SHORT).show()
            XposedBridge.log("[SpaceSwitch] Return button added successfully")
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Error adding return button: ${e.message}")
        }
    }

    private fun addSpaceListButton(activity: Activity) {
        try {
            val button = Button(activity).apply {
                text = "查看分身列表"
                textSize = 16f
                setOnClickListener {
                    val spaces = SpaceDetector.getAllSpaces(activity)
                    Toast.makeText(
                        activity,
                        "检测到 ${spaces.size} 个分身空间",
                        Toast.LENGTH_SHORT
                    ).show()
                    XposedBridge.log("[SpaceSwitch] Detected ${spaces.size} spaces: $spaces")
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
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Error adding space list button: ${e.message}")
        }
    }
}
