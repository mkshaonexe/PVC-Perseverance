package com.perseverance.pvc.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.repository.AuthRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
    
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUpWithEmail(email, password)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithEmail(email, password)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }
    
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithGoogle(data)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Google sign in failed")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            _authState.value = if (result.isSuccess) {
                AuthState.Idle
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Reset password failed")
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
