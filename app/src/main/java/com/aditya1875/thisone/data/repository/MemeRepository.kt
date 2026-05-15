// ─────────────────────────────────────────────
// data/repository/MemeRepository.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.repository

import com.aditya1875.thisone.data.local.SavedMemeDao
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.model.MemeTemplate
import com.aditya1875.thisone.data.model.SavedMeme
import com.aditya1875.thisone.data.remote.AnthropicApi
import com.aditya1875.thisone.data.remote.AnthropicMessage
import com.aditya1875.thisone.data.remote.AnthropicRequest
import com.aditya1875.thisone.data.remote.ImgflipApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class MemeRepository(
    private val imgflipApi: ImgflipApi,
    private val anthropicApi: AnthropicApi,
    private val savedMemeDao: SavedMemeDao,
    private val gson: Gson,
) {

    // ── Meme templates ─────────────────────────────────────────────────────

    /** Fetch all templates from Imgflip. Cache result in memory after first call. */
    private var cachedTemplates: List<MemeTemplate>? = null

    suspend fun getTemplates(): List<MemeTemplate> {
        cachedTemplates?.let { return it }
        return imgflipApi.getMemes().data.memes.also { cachedTemplates = it }
    }

    // ── AI matching ────────────────────────────────────────────────────────

    /**
     * Given a [situation] string, asks Claude to pick the 3 best matching
     * meme templates from [templates] and write captions for each.
     *
     * Returns a list of [MemeResult] ordered by vibe score (best first).
     */
    suspend fun matchMemes(
        situation: String,
        templates: List<MemeTemplate>,
    ): List<MemeResult> {

        // Build a compact template list for the prompt (id + name only — keeps tokens low)
        val templateSummary = templates.take(100).joinToString("\n") {
            "${it.id}|${it.name}|boxes:${it.boxCount}"
        }

        val systemPrompt = """
            You are a meme expert. Given a situation description and a list of meme templates,
            you pick the 3 most fitting memes and write captions for them.
            
            RESPOND ONLY with a valid JSON array. No markdown, no preamble, no explanation outside the JSON.
            
            JSON format (array of 3 objects):
            [
              {
                "templateId": "<id from list>",
                "topText": "<caption line 1>",
                "bottomText": "<caption line 2, empty string if single box>",
                "matchReason": "<1 sentence why this meme fits — witty, max 10 words>",
                "vibeScore": <integer 1-10>
              }
            ]
            
            Rules:
            - Captions must feel natural and funny, not forced
            - Match the emotional subtext (sarcasm, joy, frustration, etc.)
            - vibeScore 10 = perfect match, 1 = loose fit
            - Keep topText and bottomText under 60 characters each
            - If a template has boxCount 1, set bottomText to ""
        """.trimIndent()

        val userMessage = """
            Situation: "$situation"
            
            Available templates:
            $templateSummary
        """.trimIndent()

        val response = anthropicApi.createMessage(
            AnthropicRequest(
                system = systemPrompt,
                messages = listOf(AnthropicMessage(role = "user", content = userMessage)),
            )
        )

        val rawJson = response.content.firstOrNull()?.text ?: return emptyList()

        // Parse Claude's JSON response into result objects
        return parseResults(rawJson, templates)
    }

    private fun parseResults(
        rawJson: String,
        templates: List<MemeTemplate>,
    ): List<MemeResult> {
        return try {
            val type = object : TypeToken<List<MatchedMemeJson>>() {}.type
            val matched: List<MatchedMemeJson> = gson.fromJson(rawJson.trim(), type)

            val templateMap = templates.associateBy { it.id }

            matched
                .mapNotNull { item ->
                    val template = templateMap[item.templateId] ?: return@mapNotNull null
                    MemeResult(
                        template     = template,
                        topText      = item.topText,
                        bottomText   = item.bottomText,
                        matchReason  = item.matchReason,
                        vibeScore    = item.vibeScore.coerceIn(1, 10),
                    )
                }
                .sortedByDescending { it.vibeScore }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Internal JSON shape matching Claude's output
    private data class MatchedMemeJson(
        val templateId: String,
        val topText: String,
        val bottomText: String,
        val matchReason: String,
        val vibeScore: Int,
    )

    // ── Saved memes (Room) ─────────────────────────────────────────────────

    fun getSavedMemes(): Flow<List<SavedMeme>> = savedMemeDao.getAllSaved()

    suspend fun saveMeme(result: MemeResult, situation: String) {
        savedMemeDao.insert(
            SavedMeme(
                templateId   = result.template.id,
                templateName = result.template.name,
                templateUrl  = result.template.url,
                topText      = result.topText,
                bottomText   = result.bottomText,
                matchReason  = result.matchReason,
                situation    = situation,
            )
        )
    }

    suspend fun deleteSaved(meme: SavedMeme) = savedMemeDao.delete(meme)
}