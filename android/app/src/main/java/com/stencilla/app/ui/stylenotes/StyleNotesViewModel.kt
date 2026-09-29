package com.stencilla.app.ui.stylenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.StyleNoteEntity
import com.stencilla.app.data.repository.StyleNotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleNotesViewModel @Inject constructor(
    private val repo: StyleNotesRepository,
) : ViewModel() {

    val notes: StateFlow<List<StyleNoteEntity>> = repo.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.sync() }
    }

    fun create(title: String, body: String?, tags: String?) {
        viewModelScope.launch { repo.create(title, body, tags) }
    }

    fun update(entity: StyleNoteEntity, title: String, body: String?, tags: String?) {
        viewModelScope.launch { repo.update(entity, title, body, tags) }
    }

    fun delete(entity: StyleNoteEntity) {
        viewModelScope.launch { repo.delete(entity) }
    }
}
