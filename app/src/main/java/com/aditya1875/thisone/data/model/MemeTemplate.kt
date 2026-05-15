package com.aditya1875.thisone.data.model

import com.google.gson.annotations.SerializedName

/** One template from the Imgflip /get_memes endpoint */
data class MemeTemplate(
    val id: String,
    val name: String,
    val url: String,
    val width: Int,
    val height: Int,
    @SerializedName("box_count") val boxCount: Int,
)

/** Imgflip response wrapper */
data class ImgflipResponse(
    val success: Boolean,
    val data: ImgflipData,
)

data class ImgflipData(
    val memes: List<MemeTemplate>,
)