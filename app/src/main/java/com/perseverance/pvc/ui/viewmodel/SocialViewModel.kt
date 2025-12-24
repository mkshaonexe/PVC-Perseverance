package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.SocialRepository
import com.perseverance.pvc.data.SocialUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SocialUiState(
    val isSignedIn: Boolean = false,
    val currentUser: SocialUser? = null,
    val friends: List<SocialUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addFriendEmail: String = "",
    val showAddFriendDialog: Boolean = false
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SocialRepository()
    private val authRepository = com.perseverance.pvc.data.repository.AuthRepository(application)
    
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()
    
    init {
        checkSignInStatus()
    }
    
    private fun checkSignInStatus() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                isSignedIn = true,
                currentUser = SocialUser(
                    uid = user.id,
                    displayName = user.userMetadata?.get("full_name")?.toString() ?: "User",
                    email = user.email ?: "",
                    photoUrl = user.userMetadata?.get("avatar_url")?.toString() ?: ""
                )
            )
            // Start listening to friends status
            startFriendsListener()
        }
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
         viewModelScope.launch {
             _uiState.value = _uiState.value.copy(isLoading = true)
             val result = authRepository.signInWithGoogle(data)
             
             if (result.isSuccess) {
                 val user = result.getOrNull()!!
                 _uiState.value = _uiState.value.copy(
                     isSignedIn = true,
                     currentUser = SocialUser(
                         uid = user.id,
                         displayName = user.userMetadata?.get("full_name")?.toString() ?: "User",
                         email = user.email ?: "",
                         photoUrl = user.userMetadata?.get("avatar_url")?.toString() ?: ""
                     ),
                     error = null,
                     isLoading = false
                 )
                 
                 launch {
                     repository.updateCurrentUserProfile()
                 }
                 startFriendsListener()
             } else {
                 _uiState.value = _uiState.value.copy(
                     error = result.exceptionOrNull()?.message,
                     isLoading = false
                 )
             }
         }
    }
    
    fun getSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = SocialUiState(isSignedIn = false)
        }
    }
    
    private fun startFriendsListener() {
        viewModelScope.launch {
            repository.getFriendsStatusWaitList().collectLatest { friends ->
                _uiState.value = _uiState.value.copy(friends = friends)
            }
        }
    }
    
    // --- Friend Management ---
    
    fun updateAddFriendEmail(email: String) {
        _uiState.value = _uiState.value.copy(addFriendEmail = email)
    }
    
    fun showAddFriendDialog() {
        _uiState.value = _uiState.value.copy(showAddFriendDialog = true)
    }
    
    fun hideAddFriendDialog() {
        _uiState.value = _uiState.value.copy(
            showAddFriendDialog = false,
            addFriendEmail = ""
        )
    }
    
    fun sendFriendRequest() {
        val email = _uiState.value.addFriendEmail
        if (email.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.sendFriendRequest(email)
            _uiState.value = _uiState.value.copy(isLoading = false)
            
            if (result.isSuccess) {
                Toast.makeText(getApplication(), result.getOrNull(), Toast.LENGTH_SHORT).show()
                hideAddFriendDialog()
            } else {
                Toast.makeText(getApplication(), "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
