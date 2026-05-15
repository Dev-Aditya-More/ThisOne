// ─────────────────────────────────────────────
// ui/home/HomeViewModel.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.model.MemeTemplate
import com.aditya1875.thisone.data.repository.MemeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object LoadingTemplates : HomeUiState
    data object Matching : HomeUiState
    data class Success(val results: List<MemeResult>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

data class HomeFormState(
    val situationText: String = "",
    val isInputFocused: Boolean = false,
)

// ── ViewModel ─────────────────────────────────

class HomeViewModel(
    private val repository: MemeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(HomeFormState())
    val formState: StateFlow<HomeFormState> = _formState.asStateFlow()

    private var templates: List<MemeTemplate> = emptyList()

    // Saved meme IDs for bookmark icon state
    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())

    // ── User actions ──────────────────────────────────────────────────────

    fun onSituationChanged(text: String) {
        _formState.update { it.copy(situationText = text) }
        // Reset to idle if user clears input
        if (text.isBlank()) _uiState.value = HomeUiState.Idle
    }

    fun onFocusChanged(focused: Boolean) {
        _formState.update { it.copy(isInputFocused = focused) }
    }

    fun onFindMeme() {
        val situation = _formState.value.situationText.trim()
        if (situation.isBlank()) return

        viewModelScope.launch {
            try {
                // Step 1: load templates if not cached
                if (templates.isEmpty()) {
                    _uiState.value = HomeUiState.LoadingTemplates
                    templates = repository.getTemplates()
                }

                // Step 2: AI matching
                _uiState.value = HomeUiState.Matching
                val results = repository.matchMemes(situation, templates)

                _uiState.value = if (results.isEmpty()) {
                    HomeUiState.Error("Couldn't find a good match. Try describing differently?")
                } else {
                    HomeUiState.Success(results)
                }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Something went wrong. Please try again."
                )
            }
        }
    }

    fun onSaveMeme(result: MemeResult) {
        viewModelScope.launch {
            repository.saveMeme(result, _formState.value.situationText)
            _savedIds.update { it + result.template.id }
        }
    }

    fun onReset() {
        _uiState.value = HomeUiState.Idle
        _formState.value = HomeFormState()
    }

    fun isSaved(templateId: String): Boolean = _savedIds.value.contains(templateId)
}