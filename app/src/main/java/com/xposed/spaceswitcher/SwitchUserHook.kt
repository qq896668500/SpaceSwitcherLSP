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

object SwitchUserHook {

    fun hookSecuritySpace(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 使用反射获取 XC_MethodHook 类
            val xc_method_hook_class = Class.forName("de.robv.android.xposed.callbacks.XC_MethodHook")
            
            // 创建匿名类实例
            val hook = object : Any() {
                fun afterHookedMethod(param: Any?) {
                    try {
                        if (param == null) return
                        
                        // 通过反射获取参数
                        val thisObjectMethod = param::class.java.getDeclaredMethod("getThisObject")
                        thisObjectMethod.isAccessible = true
                        val activity = thisObjectMethod.invoke(param) as? Activity ?: return
                        
                        val argsMethod = param::class.java.getDeclaredMethod("getArgs")
                        argsMethod.isAccessible = true
                        val args = argsMethod.invoke(param) as? Array<*> ?: return
                        
                        val bundle = args.getOrNull(0) as? Bundle
                        val targetUserId = bundle?.getInt("params_target_user_id", -10000) ?: -10000
                        val currentUserId = SpaceDetector.getCurrentUserId()
                        
                        if (currentUserId != 0 && targetUserId == 0) {
                            addReturnButton(activity)
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("[SpaceSwitch] Error: ${e.message}")
                    }
                }
            }
            
            // Hook 方法
            XposedHelpers.findAndHookMethod(
                "com.miui.securityspace.ui.activity.SwitchUserActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java
            ) { param ->
                try {
                    param.invokeOriginalMethod()
                    hook.afterHookedMethod(param)
                } catch (e: Exception) {
                    XposedBridge.log("[SpaceSwitch] Hook error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Hook setup failed: ${e.message}")
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
