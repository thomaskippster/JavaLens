package com.example.javalens.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "snippets")
data class Snippet(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val code: String,
    val category: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
