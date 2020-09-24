package com.io.ellipse.presentation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.io.ellipse.R
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.login.LoginActivity
import com.io.ellipse.presentation.main.navigation.LogoutNavigation
import com.io.ellipse.presentation.util.NextScreenState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class MainFragment : BaseFragment<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutButton.setOnClickListener {
            execute(Dispatchers.IO) { viewModel.clearSession() }
        }
    }

    override fun handleCustomNavigation(state: NextScreenState) {
        when (state) {
            is LogoutNavigation -> {
                val activity = requireActivity()
                startActivity(LoginActivity.newIntent(activity))
                activity.finish()
            }
            else -> super.handleCustomNavigation(state)
        }
    }
}