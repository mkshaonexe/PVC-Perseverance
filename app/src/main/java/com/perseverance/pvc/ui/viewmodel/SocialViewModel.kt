package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.SocialUser
import com.perseverance.pvc.data.StudyGroup
import com.perseverance.pvc.data.GroupMemberWithStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Simple UI model (stripped of live calculation logic for now as real-time is gone)
data class GroupMemberUI(
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
    val time: String = "00:00:00",
    val isActive: Boolean = false,
    val isStudying: Boolean = false,
    val avatarResId: Int = 0
)

data class SocialUiState(
    val isSignedIn: Boolean = false,
    val currentUser: SocialUser? = null,
    val friends: List<SocialUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addFriendEmail: String = "",
    val showAddFriendDialog: Boolean = false,
    val globalMission: com.perseverance.pvc.data.Mission? = null,
    val globalMissionProgress: Long = 0,
    val customMissions: List<com.perseverance.pvc.data.Mission> = emptyList(),
    val showAddMissionDialog: Boolean = false,
    val newMissionTitle: String = "",
    val newMissionTargetHours: String = "",
    val groups: List<com.perseverance.pvc.data.StudyGroup> = emptyList(),
    val selectedGroup: com.perseverance.pvc.data.StudyGroup? = null,
    val isLoadingGroups: Boolean = false,
    val currentGroupId: String? = null,
    val hasJoinedCurrentGroup: Boolean = false,
    val realGroupMembers: List<GroupMemberWithStatus> = emptyList(),
    val groupMembers: List<GroupMemberUI> = emptyList(),
    val isLoadingMembers: Boolean = false
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    // Repositories (Local only)
    private val studyRepository = com.perseverance.pvc.data.StudyRepository(application)
    private val profileRepository = com.perseverance.pvc.data.ProfileRepository(application)
    private val missionRepository = com.perseverance.pvc.data.MissionRepository(application, studyRepository)
    
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()
    
    init {
        observeProfile()
        loadMissions()
        // No auth observation anymore
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.getProfile().collectLatest { profile ->
                if (profile.displayName.isNotEmpty()) {
                    // Update current user from local profile
                     var socialUser = SocialUser(
                        uid = "local_user",
                        displayName = profile.displayName,
                        photoUrl = profile.photoUrl,
                        bio = profile.bio
                    )
                    _uiState.value = _uiState.value.copy(currentUser = socialUser)
                }
            }
        }
    }

    // --- Mission Management (Local) ---

    private fun loadMissions() {
        viewModelScope.launch {
            launch {
                missionRepository.getGlobalMission().collectLatest { mission ->
                    _uiState.value = _uiState.value.copy(globalMission = mission)
                    // Progress tracking removed/simplified as it might have depended on cloud, 
                    // but MissionRepository seems local-ish (Room). Keeping safe calls.
                    if(mission != null) {
                         missionRepository.getMissionProgress(mission).collectLatest { progress ->
                            _uiState.value = _uiState.value.copy(globalMissionProgress = progress)
                        }
                    }
                }
            }
            launch {
                missionRepository.getCustomMissions().collectLatest { missions ->
                    _uiState.value = _uiState.value.copy(customMissions = missions)
                }
            }
        }
    }

    fun joinGlobalMission() {
        viewModelScope.launch {
            missionRepository.joinGlobalMission()
            Toast.makeText(getApplication(), "Joined Mission (Local)", Toast.LENGTH_SHORT).show()
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
        val hours = hoursStr.toIntOrNull() ?: return
        
        viewModelScope.launch {
            missionRepository.addCustomMission(title, hours, null)
            hideAddMissionDialog()
            Toast.makeText(getApplication(), "Mission Created!", Toast.LENGTH_SHORT).show()
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
            
            val photoUrl = imageUri?.toString() ?: _uiState.value.currentUser?.photoUrl ?: ""

            // Save to Local Storage Only
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
            Toast.makeText(getApplication(), "Profile updated (Local)", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Stubs/Removed Features ---

    fun loadGroups() {
        // Feature removed
        _uiState.value = _uiState.value.copy(groups = emptyList(), isLoadingGroups = false)
    }
    
    fun selectGroup(group: StudyGroup) {
        // Feature removed
    }

    fun loadGroupMembers(groupId: String) {
        // Feature removed
    }

    fun performEmailLogin(email: String, pass: String) {
        Toast.makeText(getApplication(), "Online features are disabled.", Toast.LENGTH_SHORT).show()
    }

    fun performEmailSignUp(email: String, pass: String, name: String) {
         Toast.makeText(getApplication(), "Online features are disabled.", Toast.LENGTH_SHORT).show()
    }
    
    fun signOut() {
        // No-op
    }

    fun updateAddFriendEmail(email: String) {}
    fun showAddFriendDialog() {}
    fun hideAddFriendDialog() {}
    fun sendFriendRequest() {}
    fun joinGroup(groupId: String) {}
    fun leaveGroup() {}
    fun startGroupStudy(subject: String = "") {}
    fun stopGroupStudy() {}
    fun onEnterGroupDetails(group: StudyGroup) {}
    fun onExitGroupDetails() {}
    fun onAppBackground() {}
    fun onAppResume() {}
}
