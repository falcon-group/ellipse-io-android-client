package com.io.ellipse.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.io.ellipse.presentation.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

abstract class BaseFragment<T : BaseViewModel> : Fragment(), Observer<NavigationState> {

    abstract val viewModel: T

    protected abstract val layoutResId: Int @LayoutRes get

    private lateinit var navigationStateLiveData: LiveData<NavigationState>

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = viewModel.navigateBack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationStateLiveData = viewModel.navigationState.asLiveData(Dispatchers.Main)
        navigationStateLiveData.observeForever(this)
    }

    override fun onDestroyView() {
        navigationStateLiveData.removeObserver(this)
        super.onDestroyView()
    }

    override fun onChanged(navigation: NavigationState) = when (navigation) {
        is BackState -> handleBackNavigation(navigation)
        is NextScreenState -> handleCustomNavigation(navigation)
    }

    open fun handleError(error: Throwable) {
        (requireActivity() as? BaseActivity<*>)?.handleError(error)
    }

    open fun showErrorDialog(message: String) {
        (requireActivity() as? BaseActivity<*>)?.showErrorDialog(message)
    }

    protected fun <R> Flow<ResourceState<R>>.observerResource(
        failureBlock: (Failure) -> Unit = ::handleErrorMessage,
        progressBlock: (Progress) -> Unit = { },
        successBlock: (Success<R>) -> Unit
    ) {
        asLiveData(Dispatchers.Main).observe(viewLifecycleOwner, Observer {
            when (it) {
                is Failure -> failureBlock(it)
                is Progress -> progressBlock(it)
                is Success<R> -> successBlock(it)
            }
        })
    }

    protected open fun handleBackNavigation(state: BackState) {
        (requireActivity() as? BaseActivity<*>)?.handleBackNavigation(state)
    }

    protected open fun handleCustomNavigation(state: NextScreenState) {
        (requireActivity() as? BaseActivity<*>)?.handleCustomNavigation(state)
    }

    private fun handleErrorMessage(failure: Failure) {
        when {
            failure.error != null -> handleError(failure.error)
            failure.message != null -> showErrorDialog(failure.message)
        }
    }
}