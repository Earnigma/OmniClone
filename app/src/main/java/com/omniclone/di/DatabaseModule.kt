package com.omniclone.di

import android.content.Context
import androidx.room.Room
import com.omniclone.data.CloneDao
import com.omniclone.data.OmniCloneDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OmniCloneDatabase {
        return Room.databaseBuilder(
            context,
            OmniCloneDatabase::class.java,
            "omniclone.db"
        ).build()
    }

    @Provides
    fun provideCloneDao(database: OmniCloneDatabase): CloneDao = database.cloneDao()
}
