// ─────────────────────────────────────────────
// ui/detail/MemeDetailViewModel.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.repository.MemeRepository
import com.aditya1875.thisone.data.repository.SelectedMemeStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val result: MemeResult,
    val situation: String,
    val alternatives: List<MemeResult>,
    val isSaved: Boolean,
)

class MemeDetailViewModel(
    private val repository: MemeRepository,
    private val selectedMemeStore: SelectedMemeStore,
) : ViewModel() {

    private val savedIds: StateFlow<Set<String>> = repository
        .getSavedMemes()
        .map { saved -> saved.map { it.templateId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val uiState: StateFlow<DetailUiState?> = combine(
        selectedMemeStore.state.filterNotNull(),
        savedIds,
    ) { selection, saved ->
        val current = selection.results.firstOrNull { it.template.id == selection.selectedTemplateId }
            ?: selection.results.firstOrNull()
            ?: return@combine null

        DetailUiState(
            result = current,
            situation = selection.situation,
            alternatives = selection.results.filterNot { it.template.id == current.template.id },
            isSaved = saved.contains(current.template.id),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onSelectAlternative(templateId: String) {
        selectedMemeStore.selectAlternative(templateId)
    }

    fun onToggleSave() {
        val state = uiState.value ?: return
        viewModelScope.launch {
            if (state.isSaved) {
                repository.deleteSavedByTemplateId(state.result.template.id)
            } else {
                repository.saveMeme(state.result, state.situation)
            }
        }
    }
}
