# MedVault Production Proguard / R8 Rules for Google Play Store

# Preserve Data Models and Serializable classes
-keep class com.medvault.snappill.data.model.** { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve ViewBinding classes
-keep class com.medvault.snappill.databinding.** { *; }

# CameraX Rules
-keep class androidx.camera.core.** { *; }
-dontwarn androidx.camera.core.**
-keep class androidx.camera.camera2.** { *; }
-dontwarn androidx.camera.camera2.**

# ML Kit Rules
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
