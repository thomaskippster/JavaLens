package com.javalens.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity): Long

    @Query("SELECT * FROM snippet_vault ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>
    
    @Query("SELECT * FROM snippet_vault ORDER BY timestamp DESC")
    suspend fun getAllSnippetsSync(): List<SnippetEntity>
    
    @Query("SELECT * FROM snippet_vault WHERE id = :id")
    fun getSnippetById(id: Long): Flow<SnippetEntity?>
    
    @Query("SELECT * FROM snippet_vault WHERE category = :category")
    fun getSnippetsByCategory(category: String): Flow<List<SnippetEntity>>

    @Delete
    suspend fun deleteSnippet(snippet: SnippetEntity): Int
    
    @Query("DELETE FROM snippet_vault WHERE id = :id")
    suspend fun deleteSnippetById(id: Long): Int
}
