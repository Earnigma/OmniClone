package com.omniclone.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.work.WorkManager
import com.omniclone.engine.ApkSigner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Hilt module providing the file-system-bound dependencies required by the clone engine.
 */
@Module
@InstallIn(SingletonComponent::class)
object CloneEngineModule {

    /**
     * Provide the directory where per-clone BouncyCastle keystores are persisted.
     */
    @Provides
    @Singleton
    fun provideKeystoreDir(@ApplicationContext context: Context): File {
        return File(
            context.getExternalFilesDir(null) ?: context.filesDir,
            "omniclone/keystores"
        ).apply { mkdirs() }
    }

    /**
     * Provide the APK signer using the persisted keystore directory.
     */
    @Provides
    @Singleton
    fun provideApkSigner(keystoreDir: File): ApkSigner {
        return ApkSigner(keystoreDir)
    }

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
