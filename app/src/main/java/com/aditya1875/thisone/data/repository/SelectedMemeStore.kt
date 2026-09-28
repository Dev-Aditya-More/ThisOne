// ─────────────────────────────────────────────
// data/repository/SelectedMemeStore.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.repository

import com.aditya1875.thisone.data.model.MemeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SelectedMemeState(
    val results: List<MemeResult>,
    val situation: String,
    val selectedTemplateId: String,
)

/**
 * Holds the meme the user just tapped into (plus its sibling matches) so the
 * detail screen can read it without re-running the search or serializing
 * [MemeResult] through nav arguments.
 */
class SelectedMemeStore {

    private val _state = MutableStateFlow<SelectedMemeState?>(null)
    val state: StateFlow<SelectedMemeState?> = _state.asStateFlow()

    fun select(results: List<MemeResult>, situation: String, templateId: String) {
        _state.value = SelectedMemeState(results, situation, templateId)
    }

    fun selectAlternative(templateId: String) {
        _state.update { it?.copy(selectedTemplateId = templateId) }
    }
}
