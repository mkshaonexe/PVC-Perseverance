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
import com.perseverance.pvc.data.GroupMemberWithStatus
import com.perseverance.pvc.utils.AnalyticsHelper
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * UI representation of a group member with live-updating time.
 * This is calculated client-side every second.
 */
data class GroupMemberUI(
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
    val timeSeconds: Long = 0,
    val isActive: Boolean = false,
    val isStudying: Boolean = false,
    val avatarResId: Int = 0
) {
    val time: String
        get() {
            val hours = timeSeconds / 3600
            val minutes = (timeSeconds % 3600) / 60
            val seconds = timeSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}

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
    val selectedGroup: com.perseverance.pvc.data.StudyGroup? = null,
    val isLoadingGroups: Boolean = false,
    val currentGroupId: String? = null,
    val hasJoinedCurrentGroup: Boolean = false,
    // Real Group Members (from Supabase)
    val realGroupMembers: List<GroupMemberWithStatus> = emptyList(),
    // UI Display Members (with live-calculated time)
    val groupMembers: List<GroupMemberUI> = emptyList(),
    val isLoadingMembers: Boolean = false
)

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val repository = SocialRepository()
    private val studyRepository = com.perseverance.pvc.data.StudyRepository(application)
    private val profileRepository = com.perseverance.pvc.data.ProfileRepository(application)
    private val missionRepository = com.perseverance.pvc.data.MissionRepository(application, studyRepository)
    
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()
    
    // Jobs for managing coroutines
    private var timerJob: Job? = null
    private var heartbeatJob: Job? = null
    private var refreshJob: Job? = null
    
    init {
        observeAuthStatus()
        observeProfile()
        loadMissions()
        loadGroups()
        startHeartbeat()
    }

    // --- Real-Time Group Member Functions ---

    /**
     * Start the heartbeat loop - updates last_active every 60 seconds.
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000) // 60 seconds
                if (_uiState.value.isSignedIn) {
                    repository.updateHeartbeat()
                }
            }
        }
    }

    /**
     * Start the timer update loop - recalculates live study times every second.
     */
    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000) // Every second
                updateLiveTimers()
            }
        }
    }

    /**
     * Update the UI with live-calculated study times.
     * This runs client-side every second, no DB writes.
     */
    private fun updateLiveTimers() {
        val realMembers = _uiState.value.realGroupMembers
        if (realMembers.isEmpty()) return
        
        val uiMembers = realMembers.map { member ->
            val liveSeconds = member.getLiveStudySeconds()
            GroupMemberUI(
                userId = member.userId,
                name = member.displayName,
                imageUrl = member.avatarUrl,
                timeSeconds = liveSeconds,
                isActive = member.isOnline(),
                isStudying = member.isStudying,
                avatarResId = if (member.isStudying) com.perseverance.pvc.R.drawable.study else com.perseverance.pvc.R.drawable.home
            )
        }
        
        _uiState.value = _uiState.value.copy(groupMembers = uiMembers)
    }

    /**
     * Load group members from Supabase and start timer loop.
     */
    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMembers = true)
            
            val members = repository.getGroupMembers(groupId)
            
            // Check if current user has joined this group
            val currentGroupId = repository.getCurrentGroupId()
            val hasJoined = currentGroupId == groupId
            
            _uiState.value = _uiState.value.copy(
                realGroupMembers = members,
                currentGroupId = currentGroupId,
                hasJoinedCurrentGroup = hasJoined,
                isLoadingMembers = false
            )
            
            // Start the timer loop for live updates
            startTimerLoop()
            
            // Also update UI immediately
            updateLiveTimers()
        }
    }

    /**
     * Refresh group members (called every 60 seconds as safety refresh).
     */
    private fun startSafetyRefresh(groupId: String) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000) // Every 60 seconds
                loadGroupMembers(groupId)
            }
        }
    }

    /**
     * Join a study group.
     */
    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Check and set username if missing
            val currentUser = _uiState.value.currentUser
            if (currentUser != null && currentUser.username.isBlank()) {
                val newUsername = generateUniqueUsername(currentUser.displayName)
                if (newUsername != null) {
                     repository.updateFullUserProfile(
                        displayName = currentUser.displayName,
                        photoData = null,
                        bio = currentUser.bio,
                        gender = currentUser.gender,
                        dateOfBirth = currentUser.dateOfBirth,
                        address = currentUser.address,
                        phoneNumber = currentUser.phoneNumber,
                        secondaryEmail = currentUser.secondaryEmail,
                        username = newUsername
                    )
                    // Optimistic update
                    _uiState.value = _uiState.value.copy(currentUser = currentUser.copy(username = newUsername))
                }
            }

            val result = repository.joinGroup(groupId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    hasJoinedCurrentGroup = true,
                    currentGroupId = groupId,
                    isLoading = false
                )
                Toast.makeText(getApplication(), "Joined group!", Toast.LENGTH_SHORT).show()
                AnalyticsHelper.logEvent("group_join")
                
                // Reload members to include self
                loadGroupMembers(groupId)
                startSafetyRefresh(groupId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
                Toast.makeText(getApplication(), "Failed to join group", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun generateUniqueUsername(displayName: String): String? {
        val baseName = displayName.filter { it.isLetterOrDigit() }.lowercase()
        val startName = if (baseName.isNotEmpty()) baseName else "user"
        
        // Try exact match
        if (!repository.isUsernameTaken(startName)) return startName
        
        // Try appending numbers
        for (i in 1..10) {
            val candidate = "$startName${(100..999).random()}"
            if (!repository.isUsernameTaken(candidate)) return candidate
        }
        
        // Fallback with timestamp
        return "${startName}${System.currentTimeMillis() % 10000}" 
    }

    /**
     * Leave the current group.
     */
    fun leaveGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.leaveGroup()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    hasJoinedCurrentGroup = false,
                    currentGroupId = null,
                    isLoading = false
                )
                Toast.makeText(getApplication(), "Left group", Toast.LENGTH_SHORT).show()
                AnalyticsHelper.logEvent("group_leave")
                
                // Stop refresh loop
                refreshJob?.cancel()
                
                // Reload members (to remove self from list)
                _uiState.value.selectedGroup?.let { loadGroupMembers(it.id) }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Start studying in the current group.
     */
    fun startGroupStudy(subject: String = "") {
        viewModelScope.launch {
            repository.startStudying(subject)
            Toast.makeText(getApplication(), "Started studying!", Toast.LENGTH_SHORT).show()
            AnalyticsHelper.logEvent("group_study_start")
            
            // Reload to reflect status change
            _uiState.value.selectedGroup?.let { loadGroupMembers(it.id) }
        }
    }

    /**
     * Stop studying in the current group.
     */
    fun stopGroupStudy() {
        viewModelScope.launch {
            repository.stopStudying()
            Toast.makeText(getApplication(), "Study session ended", Toast.LENGTH_SHORT).show()
            AnalyticsHelper.logEvent("group_study_stop")
            
            // Reload to reflect status change
            _uiState.value.selectedGroup?.let { loadGroupMembers(it.id) }
        }
    }

    /**
     * Called when entering a group details screen.
     */
    fun onEnterGroupDetails(group: StudyGroup) {
        _uiState.value = _uiState.value.copy(selectedGroup = group)
        loadGroupMembers(group.id)
        startSafetyRefresh(group.id)
    }

    /**
     * Called when leaving the group details screen.
     */
    fun onExitGroupDetails() {
        timerJob?.cancel()
        refreshJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedGroup = null,
            realGroupMembers = emptyList(),
            groupMembers = emptyList()
        )
    }

    /**
     * Handle app lifecycle - going to background.
     */
    fun onAppBackground() {
        viewModelScope.launch {
            repository.onAppBackground()
        }
    }

    /**
     * Handle app lifecycle - resuming from background.
     */
    fun onAppResume() {
        viewModelScope.launch {
            repository.onAppResume()
            // Reload current group if active
            _uiState.value.selectedGroup?.let { loadGroupMembers(it.id) }
        }
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
    


    fun selectGroup(group: com.perseverance.pvc.data.StudyGroup) {
        _uiState.value = _uiState.value.copy(selectedGroup = group)
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
                        
                        // Fetch remote profile and sync to local storage
                        val remoteProfile = repository.getCurrentUserProfile()
                        if (remoteProfile != null) {
                            // Merge remote data with the auth info (auth has the photoUrl from Google)
                            val finalPhotoUrl = if (remoteProfile.photoUrl.isNotEmpty()) remoteProfile.photoUrl else photoUrl.replace("\"", "")
                            
                            profileRepository.saveProfile(
                                displayName = remoteProfile.displayName.ifEmpty { displayName.replace("\"", "") },
                                photoUrl = finalPhotoUrl,
                                bio = remoteProfile.bio,
                                gender = remoteProfile.gender,
                                dateOfBirth = remoteProfile.dateOfBirth,
                                address = remoteProfile.address,
                                phoneNumber = remoteProfile.phoneNumber,
                                secondaryEmail = remoteProfile.secondaryEmail,
                                username = remoteProfile.username
                            )
                        } else {
                             // Initialize local with basic auth info if no remote profile exists
                             profileRepository.saveProfile(
                                displayName = displayName.replace("\"", ""),
                                photoUrl = photoUrl.replace("\"", ""),
                                bio = "",
                                gender = "",
                                dateOfBirth = "",
                                address = "",
                                phoneNumber = "",
                                secondaryEmail = "",
                                username = ""
                             )
                        }
                        
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

            // 1. Save to Local Storage
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

            // 2. Sync to Remote (Supabase)
            // We launch this concurrently but let the local update finish first so UI is responsive
            launch {
                val result = repository.updateFullUserProfile(
                    displayName = displayName,
                    photoData = null, // We are not uploading a new image file here, just updating the URL/Text
                    bio = bio,
                    gender = gender,
                    dateOfBirth = dateOfBirth,
                    address = address,
                    phoneNumber = phoneNumber,
                    secondaryEmail = secondaryEmail,
                    username = username
                )
                
                if (result.isFailure) {
                   // Ideally properly handle error, maybe a snackbar
                   // For now, at least we have local persistence
                }
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            Toast.makeText(getApplication(), "Profile updated!", Toast.LENGTH_SHORT).show()
            AnalyticsHelper.logEvent("profile_update_sync")
        }
    }
}

