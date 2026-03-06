package com.javalens.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.repository.SnippetRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class VaultViewModel(
    private val repository: SnippetRepository
) : ViewModel() {

    val snippets: StateFlow<ImmutableList<SnippetEntity>> = repository.getAllSnippets()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<SnippetEntity>().toImmutableList()
        )
}
