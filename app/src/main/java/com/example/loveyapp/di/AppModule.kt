package com.example.loveyapp.di

import com.example.loveyapp.security.AuthService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrentUsername

@Module
@InstallIn(ViewModelComponent::class, ActivityComponent::class, ServiceComponent::class, FragmentComponent::class)
object AppModule {
    @Provides
    @CurrentUsername
    fun provideCurrentUsername(authService: AuthService): String {
        return authService.currentUsername ?: throw IllegalStateException("User not logged in")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return com.google.gson.GsonBuilder().setPrettyPrinting().create()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthServiceEntryPoint {
    fun authService(): AuthService
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserManagerEntryPoint {
    fun userManager(): com.example.loveyapp.data.service.UserManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DataBackupManagerEntryPoint {
    fun dataBackupManager(): com.example.loveyapp.data.service.DataBackupManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LoveYaDatabaseFactoryEntryPoint {
    fun databaseFactory(): com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
}
