package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "api_id") val apiId: String,
    @Json(name = "api_hash") val apiHash: String,
    val phone: String
)

@JsonClass(generateAdapter = true)
data class SendCodeResponse(
    val status: String,
    val phone: String
)

@JsonClass(generateAdapter = true)
data class CodeRequest(
    val phone: String,
    val code: String,
    val password: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyCodeResponse(
    val status: String,
    @Json(name = "session_string") val sessionString: String? = null,
    @Json(name = "api_id") val apiId: String? = null,
    @Json(name = "api_hash") val apiHash: String? = null
)

@JsonClass(generateAdapter = true)
data class SessionRequest(
    @Json(name = "session_string") val sessionString: String,
    @Json(name = "api_id") val apiId: String,
    @Json(name = "api_hash") val apiHash: String
)

@JsonClass(generateAdapter = true)
data class ChatDto(
    val id: Long,
    val name: String,
    val username: String,
    val phone: String,
    @Json(name = "unread_count") val unreadCount: Int,
    @Json(name = "last_message") val lastMessage: String
)

@JsonClass(generateAdapter = true)
data class ChatsResponse(
    val chats: List<ChatDto>
)

interface ApiService {
    @POST("api/send-code")
    suspend fun sendCode(@Body request: LoginRequest): Response<SendCodeResponse>

    @POST("api/verify-code")
    suspend fun verifyCode(@Body request: CodeRequest): Response<VerifyCodeResponse>

    @POST("api/chats")
    suspend fun getChats(@Body request: SessionRequest): Response<ChatsResponse>
}
