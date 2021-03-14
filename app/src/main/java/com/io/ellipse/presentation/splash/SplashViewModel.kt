package com.io.ellipse.presentation.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.io.ellipse.data.utils.AppOverlayManager
import com.io.ellipse.domain.usecase.SessionExistenceUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.splash.navigation.LoginNavigation
import com.io.ellipse.presentation.splash.navigation.MainNavigation
import com.io.ellipse.presentation.splash.navigation.OverlaySettingsNavigation
import com.io.ellipse.workers.WorkersManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SplashViewModel @ViewModelInject constructor(
    private val sessionExistenceUseCase: SessionExistenceUseCase,
    private val workersManager: WorkersManager,
    private val appOverlayManager: AppOverlayManager
) : BaseViewModel() {

    init {
        if (appOverlayManager.canDrawOverlays) {
            navigateToNextScreen()
        } else {
            _navigationState.offer(OverlaySettingsNavigation())
        }
    }

    fun navigateToNextScreen() {
        sessionExistenceUseCase.retrieveCurrentSession()
            .flowOn(Dispatchers.IO)
            .map {
                when (it) {
                    true -> {
                        workersManager.startSynchronizingWork()
                        MainNavigation()
                    }
                    else -> LoginNavigation()
                }
            }
            .onEach { _navigationState.send(it) }
            .launchIn(viewModelScope)
    }

}