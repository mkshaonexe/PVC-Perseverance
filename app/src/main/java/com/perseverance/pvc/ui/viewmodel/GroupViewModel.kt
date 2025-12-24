package com.perseverance.pvc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.AuthRepository
import com.perseverance.pvc.data.GroupRepository
import com.perseverance.pvc.data.model.Group
import com.perseverance.pvc.data.model.Message
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val groupRepository = GroupRepository()

    private val _user = MutableStateFlow<UserInfo?>(null)
    val user: StateFlow<UserInfo?> = _user.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    _user.value = authRepository.getCurrentUser()
                    loadGroups()
                    loadUserGroup()
                } else {
                    _user.value = null
                    _selectedGroupId.value = null
                    _messages.value = emptyList()
                }
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _groups.value = groupRepository.getGroups()
        }
    }

    private fun loadUserGroup() {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            val userGroup = groupRepository.getUserGroup(userId)
            _selectedGroupId.value = userGroup?.groupId
            if (userGroup != null) {
                loadMessages(userGroup.groupId)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            try {
                groupRepository.joinGroup(userId, groupId)
                _selectedGroupId.value = groupId
                loadMessages(groupId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadMessages(groupId: String) {
        viewModelScope.launch {
            try {
                // Determine if this is the "Launch" group or if we load messages for all.
                // For now, load messages for the group.
                _messages.value = groupRepository.getMessages(groupId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            val groupId = _selectedGroupId.value ?: return@launch
            try {
                groupRepository.sendMessage(userId, groupId, content)
                loadMessages(groupId) // Refresh manually for now, or use realtime later
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun refreshMessages() {
        val groupId = _selectedGroupId.value ?: return
        loadMessages(groupId)
    }
}
