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

data class GroupMember(
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
    // Simulation
    val groupMembers: List<GroupMember> = emptyList()
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
        initializeSimulation()
    }

    private fun initializeSimulation() {
        // Initial Mock Data with some randomization
        val initialMembers = listOf(
            GroupMember("Tanjim Shakil", timeSeconds = 9701, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Nusrat Jahan", timeSeconds = 4359, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Ahmed Riaz", timeSeconds = 3523, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Farhana Rifa", timeSeconds = 2752, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Leonor", timeSeconds = 40579, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home),
            GroupMember("Abid Hasan", timeSeconds = 38476, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Jenifar Akter", timeSeconds = 25247, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home),
            GroupMember("Sajjad Hossain", timeSeconds = 20543, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Mehedi Hasan", timeSeconds = 17627, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home),
            GroupMember("Sumaiya Islam", timeSeconds = 16802, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home),
            GroupMember("Rafiqul Islam", timeSeconds = 16542, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Tasnim Rahman", timeSeconds = 15150, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Karim Ullah", timeSeconds = 11550, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home),
            GroupMember("Rahim Badsha", timeSeconds = 7950, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("Ayesha Siddi..", timeSeconds = 4350, isActive = true, isStudying = true, avatarResId = com.perseverance.pvc.R.drawable.study),
            GroupMember("User 12", timeSeconds = 750, isActive = false, isStudying = false, avatarResId = com.perseverance.pvc.R.drawable.home)
        )
        _uiState.value = _uiState.value.copy(groupMembers = initialMembers)
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                updateSimulation()
            }
        }
    }

    private fun updateSimulation() {
        val currentMembers = _uiState.value.groupMembers
        val updatedMembers = currentMembers.map { member ->
            var newTime = member.timeSeconds
            var newIsStudying = member.isStudying
            var newIsActive = member.isActive
            
            // Logic:
            // 1. If studying, increment time
            // 2. Random chance to stop studying (small chance)
            // 3. Random chance to start studying (if idle)
            // 4. Update status/icon based on studying state

            if (member.isStudying) {
                newTime += 1
                // Chance to stop: 0.5% per second (~once every 3 mins roughly on avg, but logic is per tick)
                // Let's make it rare so they study for a while. 0.1% chance.
                if (Math.random() < 0.001) {
                    newIsStudying = false
                    newIsActive = false // "Offline" or just not studying
                }
            } else {
                // Chance to start: 0.2% per second
                 if (Math.random() < 0.002) {
                    newIsStudying = true
                    newIsActive = true
                }
            }
            
            member.copy(
                timeSeconds = newTime, 
                isStudying = newIsStudying, 
                isActive = newIsActive,
                avatarResId = if (newIsStudying) com.perseverance.pvc.R.drawable.study else com.perseverance.pvc.R.drawable.home
            )
        }
        
        // Sort: Studying first, then by time descending? Or just keep original order for stability?
        // Reference image shows studying members in a separate list. The UI handles filtering.
        // But for the grid, maybe we want some order? 
        // For now, keep stability to avoid jumping UI.
        
        _uiState.value = _uiState.value.copy(groupMembers = updatedMembers)
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

