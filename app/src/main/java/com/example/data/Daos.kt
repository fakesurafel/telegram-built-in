package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM saved_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SavedSession>>

    @Query("SELECT * FROM saved_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<SavedSession?>

    @Query("SELECT * FROM saved_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSessionOneShot(): SavedSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SavedSession)

    @Query("UPDATE saved_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("UPDATE saved_sessions SET isActive = 1 WHERE phone = :phone")
    suspend fun activateSession(phone: String)

    @Delete
    suspend fun deleteSession(session: SavedSession)

    @Query("DELETE FROM saved_sessions")
    suspend fun clearAll()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM saved_chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<SavedChat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<SavedChat>)

    @Query("DELETE FROM saved_chats")
    suspend fun clearChats()
}
