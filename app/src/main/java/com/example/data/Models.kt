package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_sessions")
data class SavedSession(
    @PrimaryKey val phone: String,
    val apiId: String,
    val apiHash: String,
    val sessionString: String,
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_chats")
data class SavedChat(
    @PrimaryKey val id: Long,
    val name: String,
    val username: String,
    val phone: String,
    val unreadCount: Int,
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
