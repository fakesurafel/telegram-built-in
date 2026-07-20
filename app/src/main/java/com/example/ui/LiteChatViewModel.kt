package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LiteChatRepository
import com.example.data.SavedChat
import com.example.data.SavedSession
import com.example.data.VerifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LoginStep {
    ENTER_CREDENTIALS,
    ENTER_CODE
}

class LiteChatViewModel(private val repository: LiteChatRepository) : ViewModel() {

    val activeSession: StateFlow<SavedSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cachedChats: StateFlow<List<SavedChat>> = repository.cachedChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSessions: StateFlow<List<SavedSession>> = repository.savedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Inputs
    private val _apiId = MutableStateFlow("")
    val apiId = _apiId.asStateFlow()

    private val _apiHash = MutableStateFlow("")
    val apiHash = _apiHash.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _code = MutableStateFlow("")
    val code = _code.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _useSandbox = MutableStateFlow(true) // Default to true for smooth instant demo
    val useSandbox = _useSandbox.asStateFlow()

    private val _customBaseUrl = MutableStateFlow("http://10.0.2.2:8000")
    val customBaseUrl = _customBaseUrl.asStateFlow()

    // Loading & Error States
    private val _isSendingCode = MutableStateFlow(false)
    val isSendingCode = _isSendingCode.asStateFlow()

    private val _isVerifyingCode = MutableStateFlow(false)
    val isVerifyingCode = _isVerifyingCode.asStateFlow()

    private val _isFetchingChats = MutableStateFlow(false)
    val isFetchingChats = _isFetchingChats.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loginStep = MutableStateFlow(LoginStep.ENTER_CREDENTIALS)
    val loginStep = _loginStep.asStateFlow()

    private val _requiresPassword = MutableStateFlow(false)
    val requiresPassword = _requiresPassword.asStateFlow()

    fun onApiIdChanged(value: String) { _apiId.value = value }
    fun onApiHashChanged(value: String) { _apiHash.value = value }
    fun onPhoneChanged(value: String) { _phone.value = value }
    fun onCodeChanged(value: String) { _code.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }
    fun onSandboxToggled(value: Boolean) { _useSandbox.value = value }
    fun onBaseUrlChanged(value: String) { _customBaseUrl.value = value }
    fun clearError() { _error.value = null }

    fun resetStep() {
        _loginStep.value = LoginStep.ENTER_CREDENTIALS
        _requiresPassword.value = false
        _code.value = ""
        _password.value = ""
        _error.value = null
    }

    fun sendAuthenticationCode() {
        if (_apiId.value.isBlank() || _apiHash.value.isBlank() || _phone.value.isBlank()) {
            _error.value = "Please fill in all configuration fields."
            return
        }

        viewModelScope.launch {
            _isSendingCode.value = true
            _error.value = null
            
            val result = repository.sendCode(
                apiId = _apiId.value.trim(),
                apiHash = _apiHash.value.trim(),
                phone = _phone.value.trim(),
                useSandbox = _useSandbox.value,
                customUrl = _customBaseUrl.value.trim()
            )

            result.fold(
                onSuccess = {
                    _loginStep.value = LoginStep.ENTER_CODE
                },
                onFailure = {
                    _error.value = it.localizedMessage ?: "Failed to send code request"
                }
            )
            _isSendingCode.value = false
        }
    }

    fun verifyAuthenticationCode() {
        if (_code.value.isBlank()) {
            _error.value = "Please enter the Telegram authentication code."
            return
        }

        viewModelScope.launch {
            _isVerifyingCode.value = true
            _error.value = null

            val result = repository.verifyCode(
                apiId = _apiId.value.trim(),
                apiHash = _apiHash.value.trim(),
                phone = _phone.value.trim(),
                code = _code.value.trim(),
                password = _password.value.trim().ifBlank { null },
                useSandbox = _useSandbox.value,
                customUrl = _customBaseUrl.value.trim()
            )

            result.fold(
                onSuccess = { verifyResult ->
                    when (verifyResult) {
                        is VerifyResult.RequiresPassword -> {
                            _requiresPassword.value = true
                            _error.value = "Two-step verification is enabled. Please enter your cloud password."
                        }
                        is VerifyResult.Success -> {
                            _loginStep.value = LoginStep.ENTER_CREDENTIALS
                            _requiresPassword.value = false
                            _code.value = ""
                            _password.value = ""
                            // Pre-fetch chats automatically on login success
                            fetchChats()
                        }
                    }
                },
                onFailure = {
                    _error.value = it.localizedMessage ?: "Verification failed"
                }
            )
            _isVerifyingCode.value = false
        }
    }

    fun fetchChats() {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            _isFetchingChats.value = true
            _error.value = null
            
            val result = repository.fetchChats(
                session = session,
                useSandbox = _useSandbox.value,
                customUrl = _customBaseUrl.value.trim()
            )

            result.fold(
                onSuccess = {
                    // Cached automatically inside DB, Flow will stream update
                },
                onFailure = {
                    _error.value = it.localizedMessage ?: "Failed to fetch chats"
                }
            )
            _isFetchingChats.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            resetStep()
        }
    }

    fun selectSession(phone: String) {
        viewModelScope.launch {
            repository.selectSession(phone)
            // Pre-fetch chats automatically on switching sessions
            fetchChats()
        }
    }

    fun deleteSession(session: SavedSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }
}

class LiteChatViewModelFactory(private val repository: LiteChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiteChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiteChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
