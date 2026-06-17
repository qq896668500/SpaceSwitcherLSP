package com.xposed.spaceswitcher;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if ("com.miui.securityspace".equals(lpparam.packageName)) {
                XposedBridge.log("[SpaceSwitch] Hook MIUI SecuritySpace");
                SpaceSwitchHook.hookSecuritySpace(lpparam);
            }
        } catch (Exception e) {
            XposedBridge.log("[SpaceSwitch] Error: " + e.getMessage());
        }
    }
}
