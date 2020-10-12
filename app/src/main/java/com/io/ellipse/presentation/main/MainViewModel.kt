package com.io.ellipse.presentation.main

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.domain.usecase.note.DeleteNoteUseCase
import com.io.ellipse.domain.usecase.LogoutUseCase
import com.io.ellipse.domain.usecase.note.NotesPaginationUseCase
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.main.navigation.LogoutNavigation
import com.io.ellipse.presentation.main.navigation.NoteNavigation
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class MainViewModel @ViewModelInject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val notesPaginationUseCase: NotesPaginationUseCase
) : BaseViewModel() {

    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<NoteEntity>>? = null

    fun search(query: String): Flow<PagingData<NoteEntity>> {
        val lastResult = currentSearchResult
        Log.e("SEARCH", "$query")
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
        logoutUseCase.clearSession()
        _navigationState.sendBlocking(LogoutNavigation())
    }
}