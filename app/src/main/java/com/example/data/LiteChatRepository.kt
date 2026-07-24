package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class LiteChatRepository(
    private val sessionDao: SessionDao,
    private val chatDao: ChatDao
) {
    val savedSessions: Flow<List<SavedSession>> = sessionDao.getAllSessions()
    val activeSession: Flow<SavedSession?> = sessionDao.getActiveSession()
    val cachedChats: Flow<List<SavedChat>> = chatDao.getAllChats()

    suspend fun sendCode(
        apiId: String,
        apiHash: String,
        phone: String
    ): Result<String> {
        delay(800) // Simulate Telegram protocol handshake
        return if (phone.isBlank() || apiId.isBlank() || apiHash.isBlank()) {
            Result.failure(Exception("All configuration fields (API ID, API Hash, Phone) are required."))
        } else {
            Result.success("code_sent")
        }
    }

    suspend fun verifyCode(
        apiId: String,
        apiHash: String,
        phone: String,
        code: String,
        password: String?
    ): Result<VerifyResult> {
        delay(800)
        if (code.isBlank()) {
            return Result.failure(Exception("Telegram authorization code cannot be empty."))
        }

        // Generate session string
        val sessionString = "tg_session_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val session = SavedSession(
            phone = phone,
            apiId = apiId,
            apiHash = apiHash,
            sessionString = sessionString,
            isActive = true
        )
        sessionDao.deactivateAllSessions()
        sessionDao.insertSession(session)
        return Result.success(VerifyResult.Success(sessionString))
    }

    suspend fun fetchChats(
        session: SavedSession
    ): Result<List<SavedChat>> {
        delay(600)
        val mockChats = listOf(
            SavedChat(1, "Pavel Durov", "durov", "Private", 1, "Welcome to LiteChat! Direct & fast client.", System.currentTimeMillis()),
            SavedChat(2, "Mom ❤️", "No username", "+1 (555) 234-5678", 0, "Make sure you drink enough water and rest!", System.currentTimeMillis() - 1000 * 60 * 5),
            SavedChat(3, "Sarah Jenkins", "sarah_j", "+1 (555) 987-6543", 3, "Did you finish the Jetpack Compose animations?", System.currentTimeMillis() - 1000 * 60 * 30),
            SavedChat(4, "Alex Mercer", "alex_m", "+44 7911 123456", 0, "Let's grab a coffee later. I have some ideas.", System.currentTimeMillis() - 1000 * 60 * 120),
            SavedChat(5, "Design Lead", "art_director", "Private", 0, "The brand assets look perfect! Keep it up.", System.currentTimeMillis() - 1000 * 60 * 600),
            SavedChat(6, "Alice Smith", "alice_s", "+1 (555) 443-2211", 0, "Perfect, see you tomorrow!", System.currentTimeMillis() - 1000 * 60 * 1400)
        )
        chatDao.clearChats()
        chatDao.insertChats(mockChats)
        return Result.success(mockChats)
    }

    suspend fun importSessionKey(
        phone: String,
        apiId: String,
        apiHash: String,
        sessionString: String
    ): Result<SavedSession> {
        val cleanPhone = if (phone.isBlank()) "+1 (555) ${ (1000000..9999999).random() }" else phone
        val cleanApiId = if (apiId.isBlank()) "2040" else apiId
        val cleanApiHash = if (apiHash.isBlank()) "b18441a1ed607e10a4425b96a084f79d" else apiHash

        val session = SavedSession(
            phone = cleanPhone,
            apiId = cleanApiId,
            apiHash = cleanApiHash,
            sessionString = sessionString.trim(),
            isActive = true
        )
        sessionDao.deactivateAllSessions()
        sessionDao.insertSession(session)
        chatDao.clearChats()
        return Result.success(session)
    }

    suspend fun logout() {
        sessionDao.deactivateAllSessions()
        chatDao.clearChats()
    }

    suspend fun selectSession(phone: String) {
        sessionDao.deactivateAllSessions()
        sessionDao.activateSession(phone)
        chatDao.clearChats() // Clear chats so they reload for the new session
    }

    suspend fun deleteSession(session: SavedSession) {
        sessionDao.deleteSession(session)
    }
}

sealed class VerifyResult {
    object RequiresPassword : VerifyResult()
    data class Success(val sessionString: String) : VerifyResult()
}
