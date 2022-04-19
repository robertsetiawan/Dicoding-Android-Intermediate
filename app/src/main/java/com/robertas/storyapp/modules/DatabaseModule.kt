package com.robertas.storyapp.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.robertas.storyapp.abstractions.StoryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideStoryDatabase(@ApplicationContext context: Context): StoryDatabase {
        return Room.databaseBuilder(
            context,
            StoryDatabase::class.java,
            StoryDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}