package com.javalens.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippet_vault")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val codeContent: String,
    val timestamp: Long = System.currentTimeMillis()
)
