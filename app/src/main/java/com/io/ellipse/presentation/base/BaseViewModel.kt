package com.io.ellipse.presentation.base

import androidx.lifecycle.ViewModel
import com.io.ellipse.presentation.util.BackState
import com.io.ellipse.presentation.util.Failure
import com.io.ellipse.presentation.util.NavigationState
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    protected val _navigationState: BroadcastChannel<NavigationState> =
        BroadcastChannel(Channel.CONFLATED)
    protected val _errorState: BroadcastChannel<Failure> =
        BroadcastChannel(Channel.CONFLATED)

    val navigationState: Flow<NavigationState>
        get() = _navigationState.openSubscription()
            .receiveAsFlow()

    val errorState: Flow<Failure>
        get() = _errorState.openSubscription()
            .receiveAsFlow()

    open fun navigateBack() {
        _navigationState.sendBlocking(BackState())
    }

    suspend fun <T> proceed(block: suspend () -> T): T? = try {
        block()
    } catch (ex: Exception) {
        Timber.e(ex)
        _errorState.send(Failure(error = ex))
        null
    }

    class EmptyViewModel @Inject constructor() : BaseViewModel()

    override fun onCleared() {
        super.onCleared()
        _errorState.close()
        _navigationState.close()
    }
}