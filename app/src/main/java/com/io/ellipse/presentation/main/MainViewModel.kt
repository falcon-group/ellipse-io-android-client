package com.io.ellipse.presentation.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.io.ellipse.data.bluetooth.device.core.SupporterFacade
import com.io.ellipse.data.bluetooth.gatt.BluetoothConnectionHelper
import com.io.ellipse.data.network.state.NetworkStateManager
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.utils.AppBackgroundManager
import com.io.ellipse.data.utils.AppOverlayManager
import com.io.ellipse.domain.usecase.LogoutUseCase
import com.io.ellipse.domain.usecase.main.ApplicationStateUseCase
import com.io.ellipse.domain.usecase.note.DeleteNoteUseCase
import com.io.ellipse.domain.usecase.note.NotesPaginationUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.main.navigation.LogoutNavigation
import com.io.ellipse.presentation.main.navigation.NoteNavigation
import com.io.ellipse.workers.WorkersManager
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class MainViewModel @ViewModelInject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val notesPaginationUseCase: NotesPaginationUseCase,
    private val applicationStateUseCase: ApplicationStateUseCase,
    private val networkStateManager: NetworkStateManager,
    private val supporterFacade: SupporterFacade,
    private val bluetoothConnectionHelper: BluetoothConnectionHelper,
    private val appBackgroundManager: AppBackgroundManager,
    private val appOverlayManager: AppOverlayManager,
    private val workersManager: WorkersManager
) : BaseViewModel() {

    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<NoteEntity>>? = null

    init {
        networkStateManager.startTracking()
        workersManager.startSynchronizingParamWork()
        workersManager.startSynchronizingWork()
    }

    fun checkPermissions() {
        if (!appBackgroundManager.isIgnoringBatteryOptimizations) {
            appBackgroundManager.requestPermission()
        }
        if (!appOverlayManager.canDrawOverlays) {
            appOverlayManager.requestPermission()
        }
    }

    fun search(query: String): Flow<PagingData<NoteEntity>> {
        val lastResult = currentSearchResult
        if (query == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = query
        val newResult: Flow<PagingData<NoteEntity>> = notesPaginationUseCase.retrieveItems(query)
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }

    fun navigateToNoteDetails(id: String) {
        _navigationState.sendBlocking(NoteNavigation(id))
    }

    fun navigateToNoteCreation() {
        _navigationState.sendBlocking(NoteNavigation())
    }

    suspend fun delete(id: String, position: Int) = proceed {
        deleteNoteUseCase.delete(id)
    }

    suspend fun logout() = proceed {
        workersManager.cancelPeriodicalWork()
        bluetoothConnectionHelper.disconnect()
        logoutUseCase.clearSession()
        _navigationState.sendBlocking(LogoutNavigation())
    }

    fun subscribeAppState() = applicationStateUseCase.subscribeForApplicationState()

    fun subscribeForHeartRate() = supporterFacade.heartRate
}