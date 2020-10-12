package com.io.ellipse.presentation.splash

import androidx.fragment.app.viewModels
import com.io.ellipse.R
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.login.LoginActivity
import com.io.ellipse.presentation.main.MainActivity
import com.io.ellipse.presentation.splash.navigation.LoginNavigation
import com.io.ellipse.presentation.splash.navigation.MainNavigation
import com.io.ellipse.presentation.util.NextScreenState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashViewModel>() {

    override val viewModel: SplashViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_splash

    override fun handleCustomNavigation(state: NextScreenState) {
        when (state) {
            is MainNavigation -> {
                val activity = requireActivity()
                startActivity(MainActivity.newIntent(activity))
                activity.finish()
            }
            is LoginNavigation -> {
                val activity = requireActivity()
                startActivity(LoginActivity.newIntent(activity))
                activity.finish()
            }
            else -> super.handleCustomNavigation(state)
        }
    }
}