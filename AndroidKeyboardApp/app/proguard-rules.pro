# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep the InputMethodService implementation
-keep class com.trg.customkeyboard.KeyboardService {
    public *;
}

# Keep KeyboardView and related classes
-keep class android.inputmethodservice.KeyboardView { *; }
-keep class android.inputmethodservice.Keyboard { *; }
-keep class android.inputmethodservice.Keyboard$* { *; }

# Keep any classes referenced from XML
-keep class * extends android.view.View
-keep class * extends android.app.Activity
-keep class * extends android.inputmethodservice.InputMethodService

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
