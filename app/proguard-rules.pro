-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okio.**
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# Compose
-keep class androidx.compose.** { *; }
-keep class com.example.loveyapp.ui.** { *; }
-keep class com.example.loveyapp.theme.** { *; }

# Room
-keep class com.example.loveyapp.data.local.database.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * implements androidx.room.Dao { *; }
-keepclassmembers class * {
    @androidx.room.Insert *;
    @androidx.room.Query *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}

# Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class com.example.loveyapp.di.** { *; }
-keep class com.example.loveyapp.App { *; }
-keep class com.example.loveyapp.Hilt_App { *; }
-keep class com.example.loveyapp.MainActivity { *; }
-keep class com.example.loveyapp.Hilt_MainActivity { *; }
-keepnames class dagger.hilt.internal.GeneratedComponent
-keepnames class dagger.hilt.internal.GeneratedComponentManager

# Gson
-keep class com.example.loveyapp.data.model.** { *; }
-keep class com.example.loveyapp.data.entity.** { *; }
-keep class com.example.loveyapp.data.local.entity.** { *; }
-keep class com.example.loveyapp.data.export.** { *; }
-keep class com.example.loveyapp.data.cloud.dto.** { *; }
-keep class com.example.loveyapp.data.cloud.CloudBackupConfig { *; }
-keep class com.example.loveyapp.data.cloud.RemoteUserSyncService { *; }
-keep class com.example.loveyapp.data.cloud.RemoteUserSyncService$RemoteUserEntry { *; }
-keep class com.example.loveyapp.data.service.DataExportService$ExportData { *; }
-keep class com.example.loveyapp.data.service.DataImportService$ImportData { *; }
-keep class com.example.loveyapp.data.service.DataImportService$ImportResult { *; }

# Retrofit
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-keep class okhttp3.internal.** { *; }
-keep interface okhttp3.** { *; }
# 显式保留 API 接口泛型签名，防止 R8 裁剪导致反射类型解析失败
-keep class com.example.loveyapp.data.cloud.GiteeApiService { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class com.example.loveyapp.worker.** { *; }

# Security Crypto
-keep class androidx.security.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# Keep all ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep all Activity and Fragment classes
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep all Serializable and Parcelable implementations
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep kotlin metadata
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep class kotlin.Metadata { *; }