// ─────────────────────────────────────────────
// data/remote/AnthropicApi.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// ── Request shapes ────────────────────────────

data class AnthropicRequest(
    val model: String = "claude-sonnet-4-5",
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val system: String,
    val messages: List<AnthropicMessage>,
)

data class AnthropicMessage(
    val role: String,       // "user" | "assistant"
    val content: String,
)

// ── Response shapes ───────────────────────────

data class AnthropicResponse(
    val content: List<AnthropicContent>,
    val model: String,
    val usage: AnthropicUsage,
)

data class AnthropicContent(
    val type: String,   // "text"
    val text: String,
)

data class AnthropicUsage(
    @SerializedName("input_tokens")  val inputTokens: Int,
    @SerializedName("output_tokens") val outputTokens: Int,
)

// ── Interface ─────────────────────────────────

interface AnthropicApi {
    @Headers(
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01",
    )
    @POST("v1/messages")
    suspend fun createMessage(
        @Body request: AnthropicRequest,
    ): AnthropicResponse
}