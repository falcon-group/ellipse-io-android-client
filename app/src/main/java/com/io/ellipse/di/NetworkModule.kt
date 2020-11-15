package com.io.ellipse.di

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.io.ellipse.data.network.http.rest.ServiceFactory
import com.io.ellipse.data.network.http.rest.services.AuthService
import com.io.ellipse.data.network.http.rest.services.NotesService
import com.io.ellipse.data.network.state.NetworkStateManager
import com.io.ellipse.data.network.state.NetworkTracker
import com.io.ellipse.data.network.state.impl.NetworkTrackerCompat
import com.io.ellipse.data.network.state.impl.NetworkTrackerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideAuthService(serviceFactory: ServiceFactory): AuthService {
        return serviceFactory.createService(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotesService(serviceFactory: ServiceFactory): NotesService {
        return serviceFactory.createService(NotesService::class.java)
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNetworkStateManager(
        @ApplicationContext context: Context,
        manager: ConnectivityManager
    ): NetworkStateManager {
        val networkTracker: NetworkTracker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkTrackerImpl(manager)
        } else {
            NetworkTrackerCompat(manager, context)
        }
        return NetworkStateManager(networkTracker)
    }
}