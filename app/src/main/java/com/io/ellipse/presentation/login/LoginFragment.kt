package com.io.ellipse.presentation.login

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import com.io.ellipse.R
import com.io.ellipse.common.android.onDelayClick
import com.io.ellipse.domain.validation.exceptions.login.EmptyFieldException
import com.io.ellipse.domain.validation.exceptions.login.IrregularPhoneNumberException
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.login.navigation.MainNavigation
import com.io.ellipse.presentation.main.MainActivity
import com.io.ellipse.presentation.util.Failure
import com.io.ellipse.presentation.util.NextScreenState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginViewModel>() {

    override val viewModel: LoginViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_login

    private lateinit var usernameTextWatcher: TextWatcher
    private lateinit var passwordTextWatcher: TextWatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        initObservers()
    }

    override fun onDestroyView() {
        usernameEditText.removeTextChangedListener(usernameTextWatcher)
        passwordEditText.removeTextChangedListener(passwordTextWatcher)
        super.onDestroyView()
    }

    override fun handleCustomNavigation(state: NextScreenState) {
        when (state) {
            is MainNavigation -> {
                val activity = requireActivity()
                startActivity(MainActivity.newIntent(activity))
                activity.finish()
            }
            else -> super.handleCustomNavigation(state)
        }
    }

    private fun setupViews() {
        initTextInputs()
        loginButton.onDelayClick { authorize() }
    }

    private fun initTextInputs() {
        usernameTextWatcher = usernameEditText.addTextChangedListener {
            viewModel.validateUsername(it?.toString() ?: "")
        }
        passwordTextWatcher = passwordEditText.addTextChangedListener {
            viewModel.validatePassword(it?.toString() ?: "")
        }
    }

    private fun initObservers() {
        viewModel.usernameError.asLiveData(Dispatchers.Main).observe(
            viewLifecycleOwner,
            ::observeUsernameError
        )
        viewModel.passwordError.asLiveData(Dispatchers.Main).observe(
            viewLifecycleOwner,
            ::observePasswordError
        )
        viewModel.passwordError.combine(viewModel.usernameError) { password, username ->
            password == null && username == null
        }.flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main).observe(
            viewLifecycleOwner,
            ::observeLoginButtonAvailability
        )
    }

    private fun authorize() = execute(Dispatchers.IO) {
        viewModel.authorize(usernameEditText.text.toString(), passwordEditText.text.toString())
    }

    private fun observeUsernameError(failure: Failure?) {
        usernameInputLayout.error = when {
            failure?.message != null -> failure.message
            failure?.error != null -> when (failure.error) {
                is EmptyFieldException -> getString(R.string.error_phone_is_empty)
                is IrregularPhoneNumberException -> getString(R.string.error_phone_is_insufficient)
                else -> getString(R.string.error_insufficient_field)
            }
            else -> null
        }
    }

    private fun observePasswordError(failure: Failure?) {
        passwordInputLayout.error = when {
            failure?.message != null -> failure.message
            failure?.error != null -> when (failure.error) {
                is EmptyFieldException -> getString(R.string.error_password_is_empty)
                else -> getString(R.string.error_insufficient_field)
            }
            else -> null
        }
    }

    private fun observeLoginButtonAvailability(isAvailable: Boolean) {
        loginButton.isEnabled = isAvailable
    }
}