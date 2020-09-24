package com.io.ellipse.presentation.main

import androidx.fragment.app.viewModels
import com.io.ellipse.R
import com.io.ellipse.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment<MainViewModel>() {

    override val viewModel: MainViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_main

}