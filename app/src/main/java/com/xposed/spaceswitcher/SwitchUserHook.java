package com.xposed.spaceswitcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_MethodHook;

public class SwitchUserHook {

    public static void hookSecuritySpace(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.miui.securityspace.ui.activity.SwitchUserActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Activity activity = (Activity) param.thisObject;
                            Bundle bundle = (Bundle) param.args[0];
                            int targetUserId = bundle != null ? bundle.getInt("params_target_user_id", -10000) : -10000;
                            int currentUserId = SpaceDetector.INSTANCE.getCurrentUserId();
                            
                            if (currentUserId != 0 && targetUserId == 0) {
                                addReturnButton(activity);
                            }
                        } catch (Exception e) {
                            XposedBridge.log("[SpaceSwitch] Error: " + e.getMessage());
                        }
                    }
                }
            );
        } catch (Exception e) {
            XposedBridge.log("[SpaceSwitch] Hook failed: " + e.getMessage());
        }
    }

    private static void addReturnButton(Activity activity) {
        try {
            Button button = new Button(activity);
            button.setText("返回主空间");
            button.setTextSize(16);
            button.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClassName(
                    "com.miui.securityspace",
                    "com.miui.securityspace.ui.activity.SwitchUserActivity"
                );
                intent.putExtra("params_target_user_id", 0);
                activity.startActivity(intent);
                activity.finish();
            });

            LinearLayout rootView = (LinearLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rootView.addView(button, 0, params);

            Toast.makeText(activity, "✓ 返回按钮已添加", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            XposedBridge.log("[SpaceSwitch] Button error: " + e.getMessage());
        }
    }
}
