package com.io.ellipse.presentation.login

import android.util.Patterns
import androidx.hilt.lifecycle.ViewModelInject
import com.io.ellipse.domain.usecase.LoginUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.login.navigation.MainNavigation
import com.io.ellipse.presentation.util.Failure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LoginViewModel @ViewModelInject constructor(
    private val loginUseCase: LoginUseCase
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
            username.isBlank() -> Failure()
//            !username.matches(PHONE_REGEX) -> Failure()
            else -> null
        }
    }

    fun validatePassword(password: String) {
        _passwordError.value = when {
            password.isBlank() -> Failure()
//            !password.matches(Patterns.PHONE.toRegex()) -> Failure()
            else -> null
        }
    }

    suspend fun authorize(username: String, password: String) {
        try {
            loginUseCase.authorize(username, password)
            _navigationState.value = MainNavigation
        } catch (ex: Exception) {
            _errorState.value = Failure(error = ex)
        }
    }
}