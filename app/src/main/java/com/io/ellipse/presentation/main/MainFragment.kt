package com.io.ellipse.presentation.main

import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.io.ellipse.R
import com.io.ellipse.common.android.list.GridItemMarginDecoration
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.presentation.base.BaseFragment
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            viewModel.search(query).collectLatest { adapter.submitData(it) }
        }
    }
}