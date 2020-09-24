package com.io.ellipse.presentation.main

import androidx.hilt.lifecycle.ViewModelInject
import com.io.ellipse.domain.usecase.LogoutUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.main.navigation.LogoutNavigation

class MainViewModel @ViewModelInject constructor(
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel() {

    suspend fun clearSession() = proceed {
        logoutUseCase.clearSession()
        _navigationState.value = LogoutNavigation()
    }
}