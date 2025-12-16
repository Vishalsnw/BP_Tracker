# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.bptracker.data.model.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# iText PDF
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
