package com.io.ellipse.presentation.note

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.io.ellipse.R
import com.io.ellipse.common.android.onDelayClick
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.domain.validation.exceptions.base.EmptyFieldException
import com.io.ellipse.domain.validation.exceptions.note.IllegalFieldLengthException
import com.io.ellipse.presentation.base.BaseFragment
import com.io.ellipse.presentation.util.Failure
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take

@AndroidEntryPoint
class NoteFragment : BaseFragment<NoteViewModel>() {

    companion object {

        private const val KEY_ID = "note.id"

        fun arguments(id: String? = null): Bundle {
            return Bundle().also { it.putString(KEY_ID, id) }
        }

        fun newInstance(arguments: Bundle = Bundle()): NoteFragment {
            return NoteFragment().also { it.arguments = arguments }
        }
    }

    override val viewModel: NoteViewModel by viewModels()

    override val layoutResId: Int = R.layout.fragment_note

    private lateinit var titleTextWatcher: TextWatcher
    private lateinit var contentTextWatcher: TextWatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleTextWatcher = titleEditText.addTextChangedListener {
            viewModel.validateTitle(it?.toString() ?: "")
        }
        contentTextWatcher = contentEditText.addTextChangedListener {
            viewModel.validateContent(it?.toString() ?: "")
        }
        commitButton.onDelayClick { commitChanges() }
        initObservers()
    }

    override fun onDestroyView() {
        commitButton.setOnClickListener(null)
        titleEditText.removeTextChangedListener(titleTextWatcher)
        contentEditText.removeTextChangedListener(contentTextWatcher)
        super.onDestroyView()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is IllegalFieldLengthException -> showErrorDialog(getString(R.string.error_title_is_insufficient))
            else -> super.handleError(error)
        }
    }

    private fun initObservers() = with(viewModel) {
        titleError.asLiveData(viewModelScope.coroutineContext).observe(
            viewLifecycleOwner,
            ::observeTitleError
        )
        contentError.asLiveData(viewModelScope.coroutineContext).observe(
            viewLifecycleOwner,
            ::observeContentError
        )
        contentError.combine(viewModel.titleError) { password, username ->
            password == null && username == null
        }.flowOn(Dispatchers.IO).asLiveData(viewModelScope.coroutineContext).observe(
            viewLifecycleOwner,
            ::observeLoginButtonAvailability
        )
        val id = arguments?.getString(KEY_ID) ?: return@with
        viewModel.retrieveItem(id)
            .take(2)
            .flowOn(Dispatchers.IO)
            .asLiveData(viewModelScope.coroutineContext)
            .observe(viewLifecycleOwner, ::observeNote)
    }

    private fun observeTitleError(failure: Failure?) {
        titleInputLayout.error = when {
            failure?.message != null -> failure.message
            failure?.error != null -> when (failure.error) {
                is EmptyFieldException -> getString(R.string.error_title_is_empty)
                is IllegalFieldLengthException -> getString(R.string.error_title_is_insufficient)
                else -> getString(R.string.error_insufficient_field)
            }
            else -> null
        }
    }

    private fun observeContentError(failure: Failure?) {
        contentInputLayout.error = when {
            failure?.message != null -> failure.message
            failure?.error != null -> when (failure.error) {
                is IllegalFieldLengthException -> getString(R.string.error_content_is_insufficient)
                else -> getString(R.string.error_insufficient_field)
            }
            else -> null
        }
    }

    private fun observeLoginButtonAvailability(isAvailable: Boolean) {
        commitButton.isEnabled = isAvailable
    }

    private fun observeNote(noteEntity: NoteEntity) {
        titleEditText.setText(noteEntity.title)
        contentEditText.setText(noteEntity.content)
    }

    private fun commitChanges() = execute(Dispatchers.IO) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        viewModel.commit(title, content)
    }
}