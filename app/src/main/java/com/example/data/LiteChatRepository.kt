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
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
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
        customUrl: String
    ): Result<String> {
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
        customUrl: String
    ): Result<VerifyResult> {
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

    suspend fun loginWithSessionKey(
        apiId: String,
        apiHash: String,
        phone: String,
        sessionString: String
    ): Result<Unit> {
        return try {
            val session = SavedSession(
                phone = phone,
                apiId = apiId,
                apiHash = apiHash,
                sessionString = sessionString,
                isActive = true
            )
            sessionDao.deactivateAllSessions()
            sessionDao.insertSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchChats(
        session: SavedSession,
        customUrl: String
    ): Result<List<SavedChat>> {
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
