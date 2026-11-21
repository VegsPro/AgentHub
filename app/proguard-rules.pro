-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.moshi.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.vegspro.agenthub.** { *; }
