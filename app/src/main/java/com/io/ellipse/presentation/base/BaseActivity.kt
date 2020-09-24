package com.io.ellipse.presentation.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.afollestad.materialdialogs.MaterialDialog
import com.io.ellipse.R
import com.io.ellipse.presentation.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity(), Observer<NavigationState> {

    abstract val viewModel: T

    protected abstract val layoutResId: Int @LayoutRes get

    private lateinit var navigationState: LiveData<NavigationState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        navigationState = viewModel.navigationState.asLiveData(Dispatchers.IO)
        navigationState.observeForever(this)
    }

    override fun onDestroy() {
        navigationState.removeObserver(this)
        super.onDestroy()
    }

    override fun onChanged(navigation: NavigationState) = when (navigation) {
        is BackState -> handleBackNavigation(navigation)
        is NextScreenState -> handleCustomNavigation(navigation)
    }

    open fun handleBackNavigation(state: BackState) = with(state) {
        setResult(code, Intent().putExtras(toBundle()))
        finish()
        viewModel.navigationState.observerResource {

        }
    }

    open fun handleCustomNavigation(state: NextScreenState) = Unit

    open fun handleError(error: Throwable) {
        val message = when (error) {
            is UnknownHostException -> getString(R.string.error_no_connection)
            is HttpException -> when (error.code()) {
                400 -> getString(R.string.error_irregular_request)
                401 -> getString(R.string.error_inappropriate_credentials)
                else -> getString(R.string.error_network_request_issue)
            }
            else -> getString(R.string.error_internal)
        }
        showErrorDialog(message)
    }

    open fun showErrorDialog(message: String) {
        val dialog = MaterialDialog(this).show {
            title(R.string.title_error)
            message(text = message)
            positiveButton { it.dismiss() }
        }
    }

    protected fun <R> Flow<R>.observerResource(
        failureBlock: (Failure) -> Unit = ::handleErrorMessage,
        progressBlock: (Progress) -> Unit = { },
        successBlock: (Success<R>) -> Unit
    ) = map {
        Success(data = it) as ResourceState<R>
    }.catch {
        emit(Failure(error = it) as ResourceState<R>)
    }.asLiveData(Dispatchers.Main).observe(this@BaseActivity,
        Observer {
            when (it) {
                is Failure -> failureBlock(it)
                is Progress -> progressBlock(it)
                is Success<R> -> successBlock(it)
            }
        })

    private fun handleErrorMessage(failure: Failure) {
        when {
            failure.error != null -> handleError(failure.error)
            failure.message != null -> showErrorDialog(failure.message)
        }
    }
}