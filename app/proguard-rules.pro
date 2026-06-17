-keep class com.xposed.spaceswitcher.** { *; }
-keep interface com.xposed.spaceswitcher.** { *; }
-keepclasseswithmembernames class com.xposed.spaceswitcher.** {
    native <methods>;
}
-dontwarn com.xposed.**
