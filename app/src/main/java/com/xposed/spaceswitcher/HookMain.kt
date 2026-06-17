package com.xposed.spaceswitcher

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookMain : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            if (lpparam.packageName == "com.miui.securityspace") {
                XposedBridge.log("[SpaceSwitch] Hook MIUI SecuritySpace")
                SpaceSwitchHook.hookSecuritySpace(lpparam)
            }
        } catch (e: Exception) {
            XposedBridge.log("[SpaceSwitch] Error: ${e.message}")
        }
    }
}
