package com.aditya1875.thisone.data.model

/**
 * One AI-matched meme result returned to the UI.
 * Gemini picks the template and writes the caption lines.
 */
data class MemeResult(
    val template: MemeTemplate,
    val topText: String,       // caption for box 1
    val bottomText: String,    // caption for box 2 (empty string if single-box meme)
    val matchReason: String,   // short explanation — shown as a subtitle in the card
    val vibeScore: Int,        // 1-10, how strong the match is
)

