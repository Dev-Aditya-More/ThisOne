// ─────────────────────────────────────────────
// data/repository/MemeRepository.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.repository

import com.aditya1875.thisone.data.local.SavedMemeDao
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.data.model.MemeTemplate
import com.aditya1875.thisone.data.model.SavedMeme
import com.aditya1875.thisone.data.remote.GeminiApi
import com.aditya1875.thisone.data.remote.GeminiContent
import com.aditya1875.thisone.data.remote.GeminiPart
import com.aditya1875.thisone.data.remote.GeminiRequest
import com.aditya1875.thisone.data.remote.GeminiSystemInstruction
import com.aditya1875.thisone.data.remote.ImgflipApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class MemeRepository(
    private val imgflipApi: ImgflipApi,
    private val geminiApi: GeminiApi,
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
     * Given a [situation] string, asks Gemini to pick the 3 best matching
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
                "matchReason": "<1 sentence why this meme fits, witty, max 10 words>",
                "vibeScore": <integer 1-10>
              }
            ]

            Rules:
            - Captions must feel natural and funny, not forced
            - Match the emotional subtext (sarcasm, joy, frustration, etc.)
            - vibeScore 10 = perfect match, 1 = loose fit
            - Keep topText and bottomText under 60 characters each
            - If a template has boxCount 1, set bottomText to ""
            - Never use an em dash (—) anywhere in your text. Use a comma, period, or regular hyphen instead
        """.trimIndent()

        val userMessage = """
            Situation: "$situation"
            
            Available templates:
            $templateSummary
        """.trimIndent()

        val response = geminiApi.generateContent(
            request = GeminiRequest(
                systemInstruction = GeminiSystemInstruction(parts = listOf(GeminiPart(text = systemPrompt))),
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMessage)))),
            )
        )

        val rawJson = response.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: return emptyList()

        // Parse Gemini's JSON response into result objects
        return parseResults(rawJson, templates)
    }

    private fun parseResults(
        rawJson: String,
        templates: List<MemeTemplate>,
    ): List<MemeResult> {
        return try {
            val cleanJson = rawJson.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```")
                .trim()
            val type = object : TypeToken<List<MatchedMemeJson>>() {}.type
            val matched: List<MatchedMemeJson> = gson.fromJson(cleanJson, type)

            val templateMap = templates.associateBy { it.id }

            matched
                .mapNotNull { item ->
                    val template = templateMap[item.templateId] ?: return@mapNotNull null
                    MemeResult(
                        template     = template,
                        topText      = item.topText.stripEmDash(),
                        bottomText   = item.bottomText.stripEmDash(),
                        matchReason  = item.matchReason.stripEmDash(),
                        vibeScore    = item.vibeScore.coerceIn(1, 10),
                    )
                }
                .sortedByDescending { it.vibeScore }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Gemini tends to sprinkle in em dashes even when told not to, swap them for a plain comma. */
    private fun String.stripEmDash(): String = replace(" — ", ", ").replace("—", "-")

    // Internal JSON shape matching Gemini's output
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
                vibeScore    = result.vibeScore,
            )
        )
    }

    suspend fun deleteSaved(meme: SavedMeme) = savedMemeDao.delete(meme)

    suspend fun deleteSavedByTemplateId(templateId: String) = savedMemeDao.deleteByTemplateId(templateId)
}