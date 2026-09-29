# Add project specific ProGuard rules here.
-keep class com.stencilla.app.data.remote.dto.** { *; }
-keep class com.stencilla.app.data.local.db.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
