package com.io.ellipse.di

import com.io.ellipse.data.paging.CONFIG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class PagingModule {

    @Provides
    @Singleton
    fun providePageConfig() = CONFIG
}