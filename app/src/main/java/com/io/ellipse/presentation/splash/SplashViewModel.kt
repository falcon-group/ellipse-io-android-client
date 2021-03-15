package com.io.ellipse.presentation.splash

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.io.ellipse.data.utils.AppOverlayManager
import com.io.ellipse.domain.usecase.SessionExistenceUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.splash.navigation.LoginNavigation
import com.io.ellipse.presentation.splash.navigation.MainNavigation
import com.io.ellipse.workers.WorkersManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SplashViewModel @ViewModelInject constructor(
    private val sessionExistenceUseCase: SessionExistenceUseCase,
    private val workersManager: WorkersManager
) : BaseViewModel() {

    init {
        navigateToNextScreen()
    }

    fun navigateToNextScreen() {
        sessionExistenceUseCase.retrieveCurrentSession()
            .flowOn(Dispatchers.IO)
            .map {
                when (it) {
                    true -> {
                        MainNavigation()
                    }
                    else -> LoginNavigation()
                }
            }
            .onEach { _navigationState.send(it) }
            .launchIn(viewModelScope)
    }

}