package com.io.ellipse.presentation.base

import androidx.lifecycle.ViewModel
import com.io.ellipse.presentation.util.BackState
import com.io.ellipse.presentation.util.Failure
import com.io.ellipse.presentation.util.NavigationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    protected val _navigationState: MutableStateFlow<NavigationState?> = MutableStateFlow(null)
    protected val _errorState: MutableStateFlow<Failure?> = MutableStateFlow(null)

    val navigationState: Flow<NavigationState> get() = _navigationState.filterNotNull()

    val errorState: Flow<Failure> get() = _errorState.filterNotNull()

    open fun navigateBack() {
        _navigationState.value = BackState()
    }

    suspend fun execute() {

    }

    class EmptyViewModel @Inject constructor() : BaseViewModel()
}