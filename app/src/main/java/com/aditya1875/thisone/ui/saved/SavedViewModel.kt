// ─────────────────────────────────────────────
// ui/saved/SavedViewModel.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.model.MemeTemplate
import com.aditya1875.thisone.data.model.SavedMeme
import com.aditya1875.thisone.data.repository.MemeRepository
import com.aditya1875.thisone.data.repository.SelectedMemeStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val repository: MemeRepository,
    private val selectedMemeStore: SelectedMemeStore,
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

    /** Opens a saved meme in the detail screen by re-hydrating it into a [MemeResult]. */
    fun onMemeClick(meme: SavedMeme) {
        val result = MemeResult(
            template = MemeTemplate(
                id = meme.templateId,
                name = meme.templateName,
                url = meme.templateUrl,
                width = 0,
                height = 0,
                boxCount = if (meme.bottomText.isBlank()) 1 else 2,
            ),
            topText = meme.topText,
            bottomText = meme.bottomText,
            matchReason = meme.matchReason,
            vibeScore = meme.vibeScore,
        )
        selectedMemeStore.select(listOf(result), meme.situation, meme.templateId)
    }
}