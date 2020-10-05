package com.io.ellipse.di

import android.content.Context
import androidx.room.Room
import com.io.ellipse.data.persistence.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class DatabaseModule {

    companion object {
        private const val DATABASE_NAME = "app.database"
    }

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }

    @Provides
    @Singleton
    fun notesDao(database: AppDatabase) = database.notesDao

    @Provides
    @Singleton
    fun notesRemoteKeysDao(database: AppDatabase) = database.noteRemoteKeyDao

}