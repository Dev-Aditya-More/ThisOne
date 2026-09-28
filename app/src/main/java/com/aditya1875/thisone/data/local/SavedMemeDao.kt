// ─────────────────────────────────────────────
// data/local/SavedMemeDao.kt
// ─────────────────────────────────────────────
package com.aditya1875.thisone.data.local

import androidx.room.*
import com.aditya1875.thisone.data.model.SavedMeme
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMemeDao {

    @Query("SELECT * FROM saved_memes ORDER BY savedAt DESC")
    fun getAllSaved(): Flow<List<SavedMeme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meme: SavedMeme)

    @Delete
    suspend fun delete(meme: SavedMeme)

    @Query("DELETE FROM saved_memes WHERE templateId = :templateId")
    suspend fun deleteByTemplateId(templateId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_memes WHERE templateId = :id LIMIT 1)")
    suspend fun isSaved(id: String): Boolean
}