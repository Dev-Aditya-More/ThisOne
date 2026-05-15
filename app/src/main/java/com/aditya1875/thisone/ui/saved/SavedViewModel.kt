// ─────────────────────────────────────────────
// ui/saved/SavedViewModel.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya1875.thisone.data.model.SavedMeme
import com.aditya1875.thisone.data.repository.MemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val repository: MemeRepository,
) : ViewModel() {

    val savedMemes: StateFlow<List<SavedMeme>> = repository
        .getSavedMemes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun deleteMeme(meme: SavedMeme) {
        viewModelScope.launch { repository.deleteSaved(meme) }
    }
}