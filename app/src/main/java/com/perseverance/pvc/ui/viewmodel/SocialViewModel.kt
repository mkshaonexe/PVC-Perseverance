package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.AuthRepository
import com.perseverance.pvc.data.SocialRepository
import com.perseverance.pvc.data.SocialUser
import com.perseverance.pvc.data.StudyGroup
import com.perseverance.pvc.utils.AnalyticsHelper
import io.github.jan.supabase.auth.status.SessionStatus
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
    val showAddFriendDialog: Boolean = false,
    // Mission State
    val globalMission: com.perseverance.pvc.data.Mission? = null,
    val globalMissionProgress: Long = 0,
    val customMissions: List<com.perseverance.pvc.data.Mission> = emptyList(),
    val showAddMissionDialog: Boolean = false,
    val newMissionTitle: String = "",
    val newMissionTargetHours: String = "",
    // Groups
    val groups: List<com.perseverance.pvc.data.StudyGroup> = emptyList(),
    val isLoadingGroups: Boolean = false
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val repository = SocialRepository() // This might need refactoring too if it uses Firebase, but let's stick to ViewModel first
    private val studyRepository = com.perseverance.pvc.data.StudyRepository(application)
    private val profileRepository = com.perseverance.pvc.data.ProfileRepository(application)
    private val missionRepository = com.perseverance.pvc.data.MissionRepository(application, studyRepository)
    
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()
    
    init {
        observeAuthStatus()
        observeProfile()
        loadMissions()
        loadGroups()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.getProfile().collectLatest { profile ->
                if (profile.displayName.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(currentUser = profile)
                }
            }
        }
    }
    
    private fun observeAuthStatus() {
        viewModelScope.launch {
            authRepository.sessionStatus.collectLatest { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val session = status.session
                        val user = session.user
                        // Extract user info from Supabase User
                        // Note: user_metadata contains the google profile info
                        val metadata = user?.userMetadata
                        val displayName = metadata?.get("full_name")?.toString() ?: "User"
                        val photoUrl = metadata?.get("avatar_url")?.toString() ?: ""
                        
                        // Initial basic info from Auth
                        var socialUser = SocialUser(
                                uid = user?.id ?: "",
                                displayName = displayName.replace("\"", ""), // Remove quotes if JSON parsing leaves them
                                email = user?.email ?: "",
                                photoUrl = photoUrl.replace("\"", "")
                            )
                        
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = true,
                            currentUser = socialUser
                        )

                        // In local mode, we don't fetch from DB. 
                        // The observeProfile() already handles loading from DataStore.
                        
                        startFriendsListener()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = _uiState.value.copy(
                            isSignedIn = false,
                            currentUser = null,
                            friends = emptyList()
                        )
                    }
                    else -> {} // Loading or other states
                }
            }
        }
    }
    
    private fun loadMissions() {
        viewModelScope.launch {
            // Load Global Mission
            launch {
                missionRepository.getGlobalMission().collectLatest { mission ->
                    _uiState.value = _uiState.value.copy(globalMission = mission)
                    
                    // Track progress if loaded
                    missionRepository.getMissionProgress(mission).collectLatest { progress ->
                        _uiState.value = _uiState.value.copy(globalMissionProgress = progress)
                    }
                }
            }
            
            // Load Custom Missions
            launch {
                missionRepository.getCustomMissions().collectLatest { missions ->
                    _uiState.value = _uiState.value.copy(customMissions = missions)
                }
            }
        }
    }
    
    
    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGroups = true)
            try {
                val groups = repository.getGroups()
                _uiState.value = _uiState.value.copy(
                    groups = groups,
                    isLoadingGroups = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingGroups = false)
            }
        }
    }
    
    fun performGoogleLogin() {
        viewModelScope.launch {
            try {
                // Supabase internal OAuth handle
                // Since this opens a browser, we just call the repo function
                // The deep link will bring us back, and sessionStatus will update automatically.
                authRepository.signInWithGoogle() 
                AnalyticsHelper.logLogin("google")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            AnalyticsHelper.logLogout()
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
                AnalyticsHelper.logEvent("friend_request_send")
            } else {
                Toast.makeText(getApplication(), "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Mission Management ---

    fun joinGlobalMission() {
        viewModelScope.launch {
            missionRepository.joinGlobalMission()
            Toast.makeText(getApplication(), "Joined the 101 Hours Challenge!", Toast.LENGTH_SHORT).show()
            AnalyticsHelper.logMissionJoin("global_101", "101 Hours Challenge")
        }
    }

    fun showAddMissionDialog() {
        _uiState.value = _uiState.value.copy(showAddMissionDialog = true)
    }

    fun hideAddMissionDialog() {
        _uiState.value = _uiState.value.copy(
            showAddMissionDialog = false,
            newMissionTitle = "",
            newMissionTargetHours = ""
        )
    }

    fun updateNewMissionTitle(title: String) {
        _uiState.value = _uiState.value.copy(newMissionTitle = title)
    }

    fun updateNewMissionTargetHours(hours: String) {
        _uiState.value = _uiState.value.copy(newMissionTargetHours = hours)
    }

    fun createCustomMission() {
        val title = _uiState.value.newMissionTitle
        val hoursStr = _uiState.value.newMissionTargetHours
        
        if (title.isBlank() || hoursStr.isBlank()) return
        
        val hours = hoursStr.toIntOrNull()
        if (hours == null || hours <= 0) {
            Toast.makeText(getApplication(), "Please enter valid hours", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModelScope.launch {
            missionRepository.addCustomMission(title, hours, null) // No deadline for custom for now
            hideAddMissionDialog()
            Toast.makeText(getApplication(), "Mission Created!", Toast.LENGTH_SHORT).show()
            
            AnalyticsHelper.logMissionCreate(title, hours)
        }

    }

    // --- Profile Management ---

    fun updateFullProfile(
        displayName: String,
        imageUri: Uri?,
        bio: String,
        gender: String,
        dateOfBirth: String,
        address: String,
        phoneNumber: String,
        secondaryEmail: String,
        username: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // For local storage, we can save the URI string or copy the image to local storage.
            // For now, let's just use the URI string if it's available.
            val photoUrl = imageUri?.toString() ?: _uiState.value.currentUser?.photoUrl ?: ""

            profileRepository.saveProfile(
                displayName = displayName,
                photoUrl = photoUrl,
                bio = bio,
                gender = gender,
                dateOfBirth = dateOfBirth,
                address = address,
                phoneNumber = phoneNumber,
                secondaryEmail = secondaryEmail,
                username = username
            )
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            Toast.makeText(getApplication(), "Profile updated locally!", Toast.LENGTH_SHORT).show()
            AnalyticsHelper.logEvent("profile_update_local")
        }
    }
}
