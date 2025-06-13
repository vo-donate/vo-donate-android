package com.example.vo_donate.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kevin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false, // True if login/register was successful and token exists
    val navigationEvent: AuthNavigationEvent = AuthNavigationEvent.NAVIGATE_TO_LOGIN
)

enum class AuthNavigationEvent {
    NAVIGATE_TO_MAIN,
    NAVIGATE_TO_LOGIN,
    NAVIGATE_TO_REGISTER
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenUiState())
    val uiState: StateFlow<AuthScreenUiState> = _uiState.asStateFlow()

    val id = MutableStateFlow("")
    val password = MutableStateFlow("")
    val name = MutableStateFlow("")
    val introduction = MutableStateFlow("")

    init {
        checkInitialLoginStatus()
    }

    private fun checkInitialLoginStatus() {
        _uiState.update {
            it.copy(isAuthenticated = authRepository.checkInitialLoginStatus())
        }
    }

    fun onIdChange(newId: String) {
        id.value = newId
        clearErrorMessage()
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
        clearErrorMessage()
    }

    fun onNameChange(newName: String) {
        name.value = newName
        clearErrorMessage()
    }

    fun onIntroductionChange(newIntroduction: String) {
        introduction.value = newIntroduction
        clearErrorMessage()
    }

    fun login() {
        if (id.value.isBlank() || password.value.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "ID and Password cannot be empty")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(id.value.trim(), password.value.trim())

            if (result.token == "-1") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = "Error Occurred",
                        navigationEvent = AuthNavigationEvent.NAVIGATE_TO_LOGIN
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        errorMessage = null,
                        navigationEvent = AuthNavigationEvent.NAVIGATE_TO_MAIN
                    )
                }
            }
        }
    }

    fun register() {
        if (id.value.isBlank() || password.value.isBlank() || name.value.isBlank() || introduction.value.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "ID, Password, Name, and Introduction cannot be empty")
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                id = id.value.trim(),
                password = password.value.trim(),
                name = name.value.trim(),
                introduction = introduction.value.trim()
            )
            if (result.message == "Register Failed") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = result.message,
                        navigationEvent = AuthNavigationEvent.NAVIGATE_TO_REGISTER
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = null,
                        navigationEvent = AuthNavigationEvent.NAVIGATE_TO_LOGIN
                    )
                }
            }
        }
    }

    fun onLoginClick() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                navigationEvent = AuthNavigationEvent.NAVIGATE_TO_LOGIN
            )
        }
    }

    fun clearErrorMessage() {
        if (_uiState.value.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun consumeNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = AuthNavigationEvent.NAVIGATE_TO_REGISTER) }
    }
}
