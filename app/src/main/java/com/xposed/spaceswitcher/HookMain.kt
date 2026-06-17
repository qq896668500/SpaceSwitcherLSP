package com.xposed.spaceswitcher

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookMain : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            when (lpparam.packageName) {
                "com.miui.securityspace" -> {
                    XposedBridge.log("[SpaceSwitch] Hook MIUI SecuritySpace")
                    SpaceSwitchHook.hookSecuritySpace(lpparam)
                }
                "com.android.launcher3" -> {
                    XposedBridge.log("[SpaceSwitch] Hook Launcher")
                    SpaceSwitchHook.hookLauncher(lpparam)
                }
                "com.miui.home" -> {
                    XposedBridge.log("[SpaceSwitch] Hook MIUI Launcher")
                    SpaceSwitchHook.hookLauncher(lpparam)
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Error in handleLoadPackage: ${e.message}")
        }
    }
}
