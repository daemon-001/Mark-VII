# Add project specific ProGuard rules here.

# Retain line number and source file attributes for debugging and stack trace retracing
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# -------------------------------------------------------------------------
# App data models
# -------------------------------------------------------------------------
-keep class com.daemon.markvii.data.** { *; }
-keepclassmembers class com.daemon.markvii.data.** { *; }
-keep enum com.daemon.markvii.data.** { *; }
-keep enum com.daemon.markvii.** { *; }

# Keep classes annotated with @Keep or @SerializedName
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------------------------------------------------------------------------
# Retrofit 2
# -------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep all Retrofit HTTP interfaces (interfaces that use @GET, @POST, etc.)
-keep interface * {
    @retrofit2.http.GET <methods>;
    @retrofit2.http.POST <methods>;
    @retrofit2.http.PUT <methods>;
    @retrofit2.http.DELETE <methods>;
    @retrofit2.http.PATCH <methods>;
    @retrofit2.http.HEAD <methods>;
    @retrofit2.http.OPTIONS <methods>;
    @retrofit2.http.HTTP <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit's Kotlin suspend coroutines support
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.KotlinExtensions { *; }
-keep class retrofit2.KotlinExtensions$* { *; }
-dontwarn kotlin.Unit

# -------------------------------------------------------------------------
# OkHttp 3 / OkHttp 4
# -------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }

# Keep OkHttp's internal platform detection classes
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# -------------------------------------------------------------------------
# Gson
# -------------------------------------------------------------------------
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class sun.misc.Unsafe { *; }
-dontwarn com.google.gson.**

# -------------------------------------------------------------------------
# Firebase
# -------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# -------------------------------------------------------------------------
# Coroutines
# -------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# -------------------------------------------------------------------------
# Kotlin
# -------------------------------------------------------------------------
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy { *; }

# -------------------------------------------------------------------------
# iText PDF
# -------------------------------------------------------------------------
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }

# -------------------------------------------------------------------------
# MLKit Language ID
# -------------------------------------------------------------------------
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# -------------------------------------------------------------------------
# Lottie
# -------------------------------------------------------------------------
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**