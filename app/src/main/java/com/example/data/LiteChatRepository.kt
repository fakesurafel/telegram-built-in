package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class LiteChatRepository(
    private val sessionDao: SessionDao,
    private val chatDao: ChatDao
) {
    val savedSessions: Flow<List<SavedSession>> = sessionDao.getAllSessions()
    val activeSession: Flow<SavedSession?> = sessionDao.getActiveSession()
    val cachedChats: Flow<List<SavedChat>> = chatDao.getAllChats()

    private var apiService: ApiService? = null
    private var currentUrl: String = "http://10.0.2.2:8000/"

    init {
        rebuildApiService(currentUrl)
    }

    @Synchronized
    fun rebuildApiService(baseUrl: String) {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        currentUrl = cleanUrl
        try {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(ApiService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendCode(
        apiId: String,
        apiHash: String,
        phone: String,
        useSandbox: Boolean,
        customUrl: String
    ): Result<String> {
        if (useSandbox) {
            delay(1200) // Simulate network delay
            return if (phone.isBlank() || apiId.isBlank() || apiHash.isBlank()) {
                Result.failure(Exception("All configuration fields are required."))
            } else {
                Result.success("code_sent")
            }
        }

        return try {
            rebuildApiService(customUrl)
            val service = apiService ?: throw Exception("API Service not initialized")
            val response = service.sendCode(LoginRequest(apiId, apiHash, phone))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.status)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyCode(
        apiId: String,
        apiHash: String,
        phone: String,
        code: String,
        password: String?,
        useSandbox: Boolean,
        customUrl: String
    ): Result<VerifyResult> {
        if (useSandbox) {
            delay(1200)
            if (code != "12345" && code != "77777") {
                return Result.failure(Exception("Incorrect Telegram authorization code. Try '12345'."))
            }
            // If we require password
            if (password.isNullOrBlank()) {
                // Let's pretend 2FA password is required for test phone numbers ending in 9 or 0, or just always prompt to show off the feature!
                // To keep it fully interactive, let's prompt for password if none is provided.
                return Result.success(VerifyResult.RequiresPassword)
            } else if (password != "password" && password != "admin") {
                return Result.failure(Exception("Invalid 2FA password. Try 'password'."))
            }

            // Generate a secure string session
            val mockSessionString = "1BPhvYswBu3vYq8Yv..."
            val session = SavedSession(
                phone = phone,
                apiId = apiId,
                apiHash = apiHash,
                sessionString = mockSessionString,
                isActive = true
            )
            sessionDao.deactivateAllSessions()
            sessionDao.insertSession(session)
            return Result.success(VerifyResult.Success(mockSessionString))
        }

        return try {
            rebuildApiService(customUrl)
            val service = apiService ?: throw Exception("API Service not initialized")
            val response = service.verifyCode(CodeRequest(phone, code, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.status == "requires_password") {
                        Result.success(VerifyResult.RequiresPassword)
                    } else {
                        val sessionStr = body.sessionString ?: throw Exception("No session string returned")
                        val finalApiId = body.apiId ?: apiId
                        val finalApiHash = body.apiHash ?: apiHash
                        val session = SavedSession(
                            phone = phone,
                            apiId = finalApiId,
                            apiHash = finalApiHash,
                            sessionString = sessionStr,
                            isActive = true
                        )
                        sessionDao.deactivateAllSessions()
                        sessionDao.insertSession(session)
                        Result.success(VerifyResult.Success(sessionStr))
                    }
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Verification failed"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChats(
        session: SavedSession,
        useSandbox: Boolean,
        customUrl: String
    ): Result<List<SavedChat>> {
        if (useSandbox) {
            delay(1000)
            val mockChats = listOf(
                SavedChat(1, "Pavel Durov", "durov", "Private", 1, "Welcome to LiteChat! Secure, fast, and local.", System.currentTimeMillis()),
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

        return try {
            rebuildApiService(customUrl)
            val service = apiService ?: throw Exception("API Service not initialized")
            val response = service.getChats(SessionRequest(session.sessionString, session.apiId, session.apiHash))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val saved = body.chats.map {
                        SavedChat(
                            id = it.id,
                            name = it.name,
                            username = it.username,
                            phone = it.phone,
                            unreadCount = it.unreadCount,
                            lastMessage = it.lastMessage,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    chatDao.clearChats()
                    chatDao.insertChats(saved)
                    Result.success(saved)
                } else {
                    Result.failure(Exception("Empty chats response"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to retrieve chats"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
