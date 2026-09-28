// ─────────────────────────────────────────────
// data/local/MemeDatabase.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aditya1875.thisone.data.model.SavedMeme

@Database(
    entities = [SavedMeme::class],
    version = 2,
    exportSchema = false,
)
abstract class MemeDatabase : RoomDatabase() {
    abstract fun savedMemeDao(): SavedMemeDao
}