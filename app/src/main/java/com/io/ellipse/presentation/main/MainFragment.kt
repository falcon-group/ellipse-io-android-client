package com.io.ellipse.presentation.main

import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.io.ellipse.R
import com.io.ellipse.common.android.list.decorations.GridItemMarginDecoration
import com.io.ellipse.common.android.list.decorations.PositionMarginDecoration
import com.io.ellipse.data.bluetooth.connection.HeartRateData
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.domain.usecase.main.ApplicationState
import com.io.ellipse.domain.usecase.main.BluetoothDisabledState
import com.io.ellipse.domain.usecase.main.NetworkDisabledState
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.bluetooth.device.DeviceActivity
import com.io.ellipse.presentation.login.LoginActivity
import com.io.ellipse.presentation.main.navigation.LogoutNavigation
import com.io.ellipse.presentation.main.navigation.NoteNavigation
import com.io.ellipse.presentation.main.utils.NotesAdapter
import com.io.ellipse.presentation.main.utils.OnNoteInteractListener
import com.io.ellipse.presentation.note.NoteActivity
import com.io.ellipse.presentation.note.NoteFragment
import com.io.ellipse.presentation.util.NextScreenState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
@OptIn(ExperimentalPagingApi::class)
class MainFragment : BaseFragment<MainViewModel>(), OnNoteInteractListener {

    private var searchJob: Job? = null
    private val adapter: NotesAdapter = NotesAdapter(this)
    private lateinit var queryTextWatcher: TextWatcher

    override val viewModel: MainViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_logout -> {
                execute(Dispatchers.IO) { viewModel.logout() }
                true
            }
            R.id.menu_item_bluetooth_search -> {
                startActivity(DeviceActivity.newIntent(requireActivity()))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val margin = requireContext().resources.getDimensionPixelSize(R.dimen.margin_small)
        notesRecyclerView.adapter = adapter
        notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notesRecyclerView.addItemDecoration(GridItemMarginDecoration(margin, margin, 1))

        addNoteButton.setOnClickListener { viewModel.navigateToNoteCreation() }
        queryTextWatcher = queryEditText.addTextChangedListener {
            search(it?.toString() ?: "")
        }
        search(queryEditText.text.toString())
        viewModel.subscribeAppState()
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::onApplicationStateChange))
        viewModel.subscribeForHeartRate()
            .asLiveData(Dispatchers.IO)
            .observe(viewLifecycleOwner, Observer(::onHeartRateChanged))
    }

    override fun onDestroyView() {
        queryEditText.removeTextChangedListener(queryTextWatcher)
        addNoteButton.setOnClickListener(null)
        super.onDestroyView()
    }

    override fun handleCustomNavigation(state: NextScreenState) {
        when (state) {
            is LogoutNavigation -> {
                val activity = requireActivity()
                startActivity(LoginActivity.newIntent(activity))
                activity.finish()
            }
            is NoteNavigation -> {
                val activity = requireActivity()
                val arguments = NoteFragment.arguments(state.id)
                startActivity(NoteActivity.newIntent(activity, arguments))
            }
            else -> super.handleCustomNavigation(state)
        }
    }

    override fun onItemRemove(note: NoteEntity, position: Int) {
        execute(Dispatchers.IO) { viewModel.delete(note.id, position) }
    }

    override fun onItemClick(item: NoteEntity, position: Int) {
        viewModel.navigateToNoteDetails(item.id)
    }

    private fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.search(query).collect {
                adapter.submitData(it)
            }
        }
    }

    private fun onApplicationStateChange(state: ApplicationState) {
        val (icon, text) = when (state) {
            is BluetoothDisabledState -> R.drawable.ic_bluetooth_disabled to R.string.title_bluetooth_turned_off
            is NetworkDisabledState -> R.drawable.ic_no_internet to R.string.title_network_turned_off
            else -> R.drawable.ic_sync_correct to R.string.title_hardware_active
        }
        appStateImageView.setImageResource(icon)
        appStateTextView.text = getString(text)
    }

    private fun onHeartRateChanged(rate: HeartRateData) {
        lastHeartRateTextView.text = getString(R.string.placeholder_last_heart_rate, rate.heartRate)
    }
}