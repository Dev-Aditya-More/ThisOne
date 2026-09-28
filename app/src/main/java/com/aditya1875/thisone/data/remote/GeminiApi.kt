// ─────────────────────────────────────────────
// data/remote/GeminiApi.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// ── Request shapes ────────────────────────────

data class GeminiRequest(
    @SerializedName("system_instruction") val systemInstruction: GeminiSystemInstruction,
    val contents: List<GeminiContent>,
)

data class GeminiSystemInstruction(
    val parts: List<GeminiPart>,
)

data class GeminiContent(
    val role: String,       // "user" | "model"
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)

// ── Response shapes ───────────────────────────

data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
)

data class GeminiCandidate(
    val content: GeminiContent,
)

// ── Interface ─────────────────────────────────

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-flash-lite-latest",
        @Body request: GeminiRequest,
    ): GeminiResponse
}
