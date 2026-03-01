package com.example.javalens.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Query("SELECT * FROM snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<Snippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: Snippet)

    @Delete
    suspend fun deleteSnippet(snippet: Snippet)
}

@Database(entities = [Snippet::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao
}
