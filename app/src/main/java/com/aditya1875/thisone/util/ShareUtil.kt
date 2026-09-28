// ─────────────────────────────────────────────
// util/ShareUtil.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.util

import android.content.Context
import android.content.Intent
import com.aditya1875.thisone.data.model.MemeResult

fun shareMeme(context: Context, result: MemeResult) {
    val caption = listOf(result.topText, result.bottomText).filter { it.isNotBlank() }.joinToString(" / ")
    val shareText = "$caption\n\n${result.template.url}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share this meme"))
}
