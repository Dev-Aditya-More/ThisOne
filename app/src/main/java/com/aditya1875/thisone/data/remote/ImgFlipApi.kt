// ─────────────────────────────────────────────
// data/remote/ImgflipApi.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.remote

import com.aditya1875.thisone.data.model.ImgflipResponse
import retrofit2.http.GET

interface ImgflipApi {
    @GET("get_memes")
    suspend fun getMemes(): ImgflipResponse
}
