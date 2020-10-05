package com.io.ellipse.di

import com.io.ellipse.data.network.http.rest.ServiceFactory
import com.io.ellipse.data.network.http.rest.services.AuthService
import com.io.ellipse.data.network.http.rest.services.NotesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideAuthService(serviceFactory: ServiceFactory) : AuthService {
        return serviceFactory.createService(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotesService(serviceFactory: ServiceFactory) : NotesService {
        return serviceFactory.createService(NotesService::class.java)
    }
}