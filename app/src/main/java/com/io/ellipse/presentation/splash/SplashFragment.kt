package com.io.ellipse.presentation.splash

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.io.ellipse.R
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.login.LoginActivity
import com.io.ellipse.presentation.main.MainActivity
import com.io.ellipse.presentation.splash.navigation.LoginNavigation
import com.io.ellipse.presentation.splash.navigation.MainNavigation
import com.io.ellipse.presentation.splash.navigation.OverlaySettingsNavigation
import com.io.ellipse.presentation.util.NextScreenState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashViewModel>() {

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.navigateToNextScreen()
    }

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
            is OverlaySettingsNavigation -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    val uri = Uri.parse("package:" + requireContext().packageName)
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                    resultLauncher.launch(intent)
                } else {
                    viewModel.navigateToNextScreen()
                }
            }
            else -> super.handleCustomNavigation(state)
        }
    }
}