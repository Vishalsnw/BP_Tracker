# Add project specific ProGuard rules here.

# Keep Room entities and DAOs
-keep class com.bptracker.data.model.** { *; }
-keep class com.bptracker.data.database.** { *; }
-keep class com.bptracker.data.repository.** { *; }

# Keep ViewModels
-keep class com.bptracker.ui.screens.**.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Room
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
}

# Keep Kotlin parcelize
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * implements android.os.Parcelable { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# iText PDF
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# SLF4J (used by iText)
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# BouncyCastle (used by iText for encryption)
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# Vico Charts
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# Keep Application class
-keep class com.bptracker.BloodPressureApp { *; }

# Keep receivers and widgets
-keep class com.bptracker.utils.** { *; }
-keep class com.bptracker.widget.** { *; }

# Keep R class
-keep class com.bptracker.R$* { *; }

# Kotlinx datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# Java time
-keep class java.time.** { *; }
