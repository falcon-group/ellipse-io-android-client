package com.io.ellipse.presentation.login

import android.os.Bundle
import androidx.activity.viewModels
import com.io.ellipse.R
import com.io.ellipse.presentation.base.BaseActivity
import com.io.ellipse.presentation.base.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<BaseViewModel.EmptyViewModel>() {

    override val viewModel: BaseViewModel.EmptyViewModel by viewModels()

    override val layoutResId: Int = R.layout.activity_with_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutFragment, LoginFragment())
            .commit()
    }
}