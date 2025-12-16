# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.bptracker.data.model.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# iText PDF
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# SLF4J (used by iText)
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# BouncyCastle (used by iText for encryption)
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }
