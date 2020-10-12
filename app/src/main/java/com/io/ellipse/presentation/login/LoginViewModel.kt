package com.io.ellipse.presentation.login

import android.util.Patterns
import androidx.hilt.lifecycle.ViewModelInject
import com.io.ellipse.domain.usecase.LoginUseCase
import com.io.ellipse.domain.validation.exceptions.base.EmptyFieldException
import com.io.ellipse.domain.validation.exceptions.login.IrregularPhoneNumberException
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.login.navigation.MainNavigation
import com.io.ellipse.presentation.util.Failure
import com.io.ellipse.workers.WorkersManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LoginViewModel @ViewModelInject constructor(
    private val loginUseCase: LoginUseCase,
    private val workersManager: WorkersManager
) : BaseViewModel() {

    companion object {
        private val PHONE_REGEX = Patterns.PHONE.toRegex()
    }

    private val _usernameError: MutableStateFlow<Failure?> = MutableStateFlow(null)
    private val _passwordError: MutableStateFlow<Failure?> = MutableStateFlow(null)

    val usernameError: Flow<Failure?> get() = _usernameError
    val passwordError: Flow<Failure?> get() = _passwordError

    fun validateUsername(username: String) {
        _usernameError.value = when {
            username.isBlank() -> Failure(error = EmptyFieldException())
            !username.matches(PHONE_REGEX) -> Failure(error = IrregularPhoneNumberException())
            else -> null
        }
    }

    fun validatePassword(password: String) {
        _passwordError.value = when {
            password.isBlank() -> Failure(error = EmptyFieldException())
            else -> null
        }
    }

    suspend fun authorize(username: String, password: String) = proceed {
        loginUseCase.authorize(username, password)
        workersManager.startSynchronizingWork()
        _navigationState.send(MainNavigation())
    }
}