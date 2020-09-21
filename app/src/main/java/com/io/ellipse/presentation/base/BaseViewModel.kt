package com.io.ellipse.presentation.base

import androidx.lifecycle.ViewModel
import com.io.ellipse.presentation.util.BackState
import com.io.ellipse.presentation.util.NavigationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

abstract class BaseViewModel : ViewModel() {

    protected val _navigationState: MutableStateFlow<NavigationState?> = MutableStateFlow(null)

    val navigationState: Flow<NavigationState> get() = _navigationState.filterNotNull()

    open fun navigateBack() {
        _navigationState.value = BackState()
    }

    class EmptyViewModel constructor() : BaseViewModel()
}