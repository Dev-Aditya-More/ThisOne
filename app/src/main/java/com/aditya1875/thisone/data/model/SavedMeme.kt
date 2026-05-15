package com.aditya1875.thisone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_memes")
data class SavedMeme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val templateId: String,
    val templateName: String,
    val templateUrl: String,
    val topText: String,
    val bottomText: String,
    val matchReason: String,
    val situation: String,          // the user's original description
    val savedAt: Long = System.currentTimeMillis(),
)