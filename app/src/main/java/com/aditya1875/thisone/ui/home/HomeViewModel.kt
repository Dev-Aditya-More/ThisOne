// ─────────────────────────────────────────────
// ui/home/HomeViewModel.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.model.MemeTemplate
import com.aditya1875.thisone.data.repository.MemeRepository
import com.aditya1875.thisone.data.repository.SelectedMemeStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
    private val selectedMemeStore: SelectedMemeStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(HomeFormState())
    val formState: StateFlow<HomeFormState> = _formState.asStateFlow()

    private var templates: List<MemeTemplate> = emptyList()

    // Saved meme IDs for bookmark icon state, sourced from Room so it stays correct
    // across app restarts and reflects deletions made from the Saved screen.
    private val _savedIds: StateFlow<Set<String>> = repository
        .getSavedMemes()
        .map { saved -> saved.map { it.templateId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

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
                _uiState.value = HomeUiState.Error(e.toFriendlyMessage())
            }
        }
    }

    /** Turns raw network/parsing exceptions into copy a non-developer can act on. */
    private fun Exception.toFriendlyMessage(): String = when (this) {
        is UnknownHostException, is java.net.ConnectException ->
            "No internet connection. Check your WiFi or data and try again."

        is SocketTimeoutException ->
            "That took a while and timed out. Check your connection and try again."

        is HttpException -> when (code()) {
            401, 403 -> "The AI couldn't be reached, the API key looks invalid or missing."
            404 -> "The AI couldn't be reached, its model wasn't found. Try again shortly."
            429 -> "Too many requests right now. Wait a few seconds and try again."
            in 500..599 -> "The AI is having a moment. Try again shortly."
            else -> "Something went wrong talking to the AI. Try again?"
        }

        is IOException -> "Network hiccup. Please check your connection and try again."

        else -> "Something went wrong. Please try again."
    }

    fun onSaveMeme(result: MemeResult) {
        viewModelScope.launch {
            if (isSaved(result.template.id)) {
                repository.deleteSavedByTemplateId(result.template.id)
            } else {
                repository.saveMeme(result, _formState.value.situationText)
            }
        }
    }

    fun onReset() {
        _uiState.value = HomeUiState.Idle
        _formState.value = HomeFormState()
    }

    fun isSaved(templateId: String): Boolean = _savedIds.value.contains(templateId)

    /** Stash the current results + the tapped meme so the detail screen can read them. */
    fun onMemeClick(result: MemeResult) {
        val results = (uiState.value as? HomeUiState.Success)?.results ?: listOf(result)
        selectedMemeStore.select(results, _formState.value.situationText, result.template.id)
    }
}