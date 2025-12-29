package com.perseverance.pvc.data

import android.util.Log
import com.perseverance.pvc.di.SupabaseModule
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocialUser(
    val id: String = "",
    val uid: String = "", // Keeping for compatibility, likely same as id
    @SerialName("display_name") val displayName: String = "",
    val email: String = "",
    @SerialName("avatar_url") val photoUrl: String = "",
    val status: String = "IDLE", // IDLE, STUDYING
    @SerialName("current_subject") val currentSubject: String = "",
    @SerialName("last_active") val lastActive: Long = 0,
    @SerialName("study_start_time") val studyStartTime: Long = 0,
    @SerialName("study_duration") val studyDuration: Long = 0,
    val bio: String = "",
    val gender: String = "",
    @SerialName("date_of_birth") val dateOfBirth: String = "",
    val address: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("secondary_email") val secondaryEmail: String = "",
    val username: String = ""
)

@Serializable
data class FriendRequest(
    val id: String = "",
    @SerialName("from_user_id") val fromUid: String = "",
    @SerialName("to_user_id") val toUid: String = "",
    val status: String = "PENDING"
)

@Serializable
data class FriendRelation(
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String
)

class SocialRepository {
    private val client = SupabaseModule.client
    private val TAG = "SocialRepository"

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val count = client.from("users").select {
                filter { eq("username", username) }
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            }.countOrNull() ?: 0
            
            count > 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username availability", e)
            true // Assume taken on error to be safe
        }
    }

    // --- User Management ---

    // Create or update user profile upon login
    suspend fun updateCurrentUserProfile() {
        val user = client.auth.currentUserOrNull() ?: return
        
        // Supabase Triggers handle creation, but we might want to update last_active
        try {
            client.from("users").update(
                {
                    set("last_active", System.currentTimeMillis())
                }
            ) {
                filter {
                    eq("id", user.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
        }
    }

    suspend fun getCurrentUserProfile(): SocialUser? {
        val user = client.auth.currentUserOrNull() ?: return null
        return try {
            client.from("users").select {
                filter { eq("id", user.id) }
            }.decodeSingleOrNull<SocialUser>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching current user profile", e)
            null
        }
    }

    suspend fun updateUserProfile(displayName: String, photoData: ByteArray?): Result<Unit> {
        val user = client.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))

        return try {
            var photoUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "") ?: ""

            if (photoData != null) {
                val fileName = "${user.id}/avatar_${System.currentTimeMillis()}.jpg"
                val bucket = client.storage.from("avatars")
                bucket.upload(fileName, photoData) {
                    upsert = true
                }
                photoUrl = bucket.publicUrl(fileName)
            }

            // Update Auth User Metadata
            client.auth.updateUser {
                data = JsonObject(
                    mapOf(
                        "full_name" to JsonPrimitive(displayName),
                        "avatar_url" to JsonPrimitive(photoUrl)
                    )
                )
            }

            // Sync with 'users' table (Manually, in case triggers fail/lag)
            client.from("users").update(
                {
                    set("display_name", displayName)
                    set("avatar_url", photoUrl)
                }
            ) {
                filter { eq("id", user.id) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateFullUserProfile(
        displayName: String,
        photoData: ByteArray?,
        bio: String,
        gender: String,
        dateOfBirth: String,
        address: String,
        phoneNumber: String,
        secondaryEmail: String,
        username: String
    ): Result<Unit> {
        val user = client.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))

        return try {
            var photoUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "") ?: ""

            if (photoData != null) {
                val fileName = "${user.id}/avatar_${System.currentTimeMillis()}.jpg"
                val bucket = client.storage.from("avatars")
                bucket.upload(fileName, photoData) {
                    upsert = true
                }
                photoUrl = bucket.publicUrl(fileName)
            }

            // Update Auth User Metadata (only standard fields usually, but custom data can be added)
            client.auth.updateUser {
                data = JsonObject(
                    mapOf(
                        "full_name" to JsonPrimitive(displayName),
                        "avatar_url" to JsonPrimitive(photoUrl)
                        // Note: Other fields are better kept in the users table to avoid bloating auth metadata
                    )
                )
            }

            // Sync with 'users' table
            client.from("users").update(
                {
                    set("display_name", displayName)
                    set("avatar_url", photoUrl)
                    set("bio", bio)
                    set("gender", gender)
                    set("date_of_birth", dateOfBirth)
                    set("address", address)
                    set("phone_number", phoneNumber)
                    set("secondary_email", secondaryEmail)
                    set("username", username)
                }
            ) {
                filter { eq("id", user.id) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating full user profile", e)
            Result.failure(e)
        }
    }

    // --- Status Signaling ---

    suspend fun setStatusStudying(subject: String, durationSeconds: Long) {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            client.from("users").update(
                {
                    set("status", "STUDYING")
                    set("current_subject", subject)
                    set("study_start_time", System.currentTimeMillis())
                    set("study_duration", durationSeconds)
                }
            ) {
                filter { eq("id", user.id) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    suspend fun setStatusIdle() {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            client.from("users").update(
                {
                    set("status", "IDLE")
                    set("study_start_time", 0)
                }
            ) {
                filter { eq("id", user.id) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // --- Friend System ---

    suspend fun sendFriendRequest(email: String): Result<String> {
        val currentUser = client.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))
        
        try {
            // 1. Find user by email
            val users = client.from("users").select {
                filter { eq("email", email) }
            }.decodeList<SocialUser>()
            
            if (users.isEmpty()) {
                return Result.failure(Exception("User not found"))
            }
            
            val targetUser = users.first()
            if (targetUser.id == currentUser.id) {
                return Result.failure(Exception("Cannot add yourself"))
            }

            // 2. Check my friend count (Optional, implemented via count)
            val friendCount = client.from("friends").select {
                filter { eq("user_id", currentUser.id) }
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            }.countOrNull() ?: 0
            
            if (friendCount >= 10) {
                return Result.failure(Exception("You have reached the 10 friend limit."))
            }

            // 3. Send Request
            // Check if request already exists
            val existing = client.from("friend_requests").select {
                filter {
                    eq("from_user_id", currentUser.id)
                    eq("to_user_id", targetUser.id)
                }
            }.decodeList<FriendRequest>()
            
            if (existing.isNotEmpty()) {
                 return Result.failure(Exception("Request already sent"))
            }

            client.from("friend_requests").insert(
                FriendRequest(
                    fromUid = currentUser.id,
                    toUid = targetUser.id,
                    status = "PENDING"
                )
            )
                
            return Result.success("Request sent to ${targetUser.displayName}")
        } catch (e: Exception) {
             return Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    suspend fun acceptFriendRequest(requestFromUid: String) {
        val currentUser = client.auth.currentUserOrNull() ?: return
        
        try {
            // Add Friend Relation (Both ways)
            client.from("friends").insert(
                listOf(
                    FriendRelation(userId = currentUser.id, friendId = requestFromUid),
                    FriendRelation(userId = requestFromUid, friendId = currentUser.id)
                )
            )
            
            // Delete Request
            client.from("friend_requests").delete {
                filter {
                    eq("from_user_id", requestFromUid)
                    eq("to_user_id", currentUser.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request", e)
        }
    }

    // --- Real-time Listeners (Simulated with Flow for now) ---

    fun getFriendsStatusWaitList(): Flow<List<SocialUser>> = flow {
        val currentUser = client.auth.currentUserOrNull() ?: return@flow
        
        try {
            // 1. Get Friend IDs
            val relations = client.from("friends").select {
                filter { eq("user_id", currentUser.id) }
            }.decodeList<FriendRelation>()
            
            val friendIds = relations.map { it.friendId }
            
            if (friendIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // 2. Get Users
            val friends = client.from("users").select {
                filter { isIn("id", friendIds) }
            }.decodeList<SocialUser>()
            
            emit(friends)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting friends", e)
            emit(emptyList())
        }
    }
    // --- Group Management ---

    suspend fun getGroups(): List<StudyGroup> {
        return try {
            val groups = client.from("groups").select().decodeList<StudyGroup>()
            Log.d(TAG, "getGroups: Fetched ${groups.size} groups")
            groups.forEach { group ->
                 Log.d(TAG, "getGroups: ID=${group.id}, Name=${group.name}")
            }
            groups
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching groups", e)
            emptyList()
        }
    }

    // --- Real-Time Group Study Functions ---

    /**
     * Join a study group. User can only be in one group at a time.
     */
    suspend fun joinGroup(groupId: String): Result<Unit> {
        val user = client.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            // First, leave any existing group
            client.from("group_members").delete {
                filter { eq("user_id", user.id) }
            }
            
            // Then join the new group
            client.from("group_members").insert(
                GroupMemberRecord(groupId = groupId, userId = user.id)
            )
            
            Log.d(TAG, "Joined group: $groupId")
            
            // Increment member count
            // Note: In a real app, use an RPC or Trigger. Here we do a simple read-modify-write for speed.
            try {
                val group = client.from("groups").select {
                    filter { eq("id", groupId) }
                }.decodeSingleOrNull<StudyGroup>()
                
                if (group != null) {
                    client.from("groups").update(
                        {
                            set("member_count", group.memberCount + 1)
                        }
                    ) {
                        filter { eq("id", groupId) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error incrementing member count", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining group", e)
            Result.failure(e)
        }
    }

    /**
     * Leave the current study group.
     */
    suspend fun leaveGroup(): Result<Unit> {
        val user = client.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            // Get current group ID before leaving
            val groupId = getCurrentGroupId()

            client.from("group_members").delete {
                filter { eq("user_id", user.id) }
            }
            Log.d(TAG, "Left group")
            
            // Decrement member count if we were in a group
            if (groupId != null) {
                try {
                    val group = client.from("groups").select {
                        filter { eq("id", groupId) }
                    }.decodeSingleOrNull<StudyGroup>()
                    
                    if (group != null && group.memberCount > 0) {
                        client.from("groups").update(
                            {
                                set("member_count", group.memberCount - 1)
                            }
                        ) {
                            filter { eq("id", groupId) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decrementing member count", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving group", e)
            Result.failure(e)
        }
    }

    /**
     * Get the current user's joined group ID.
     */
    suspend fun getCurrentGroupId(): String? {
        val user = client.auth.currentUserOrNull() ?: return null
        
        return try {
            val membership = client.from("group_members").select {
                filter { eq("user_id", user.id) }
            }.decodeSingleOrNull<GroupMemberRecord>()
            membership?.groupId
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current group", e)
            null
        }
    }

    /**
     * Get all members of a specific group with their study status.
     */
    /**
     * Get all members of a specific group with their study status.
     */
    suspend fun getGroupMembers(groupId: String): List<GroupMemberWithStatus> {
        return try {
            // Use the SQL View for efficient fetching
            client.from("view_group_members_status").select {
                filter { eq("group_id", groupId) }
            }.decodeList<GroupMemberWithStatus>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching group members", e)
            emptyList()
        }
    }

    /**
     * Update the user's last_active timestamp (heartbeat).
     * Should be called every 60 seconds.
     */
    suspend fun updateHeartbeat() {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            client.from("users").update(
                {
                    set("last_active", System.currentTimeMillis())
                }
            ) {
                filter { eq("id", user.id) }
            }
            Log.d(TAG, "Heartbeat updated")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating heartbeat", e)
        }
    }

    /**
     * Start studying - updates status and study_start_time.
     */
    suspend fun startStudying(subject: String = "") {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            client.from("users").update(
                {
                    set("status", "STUDYING")
                    set("study_start_time", System.currentTimeMillis())
                    set("current_subject", subject)
                    set("last_active", System.currentTimeMillis())
                }
            ) {
                filter { eq("id", user.id) }
            }
            Log.d(TAG, "Started studying: $subject")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting study", e)
        }
    }

    /**
     * Stop studying - updates status and accumulates study_duration.
     */
    suspend fun stopStudying() {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            // First, get current study_start_time to calculate duration
            val currentUser = client.from("users").select {
                filter { eq("id", user.id) }
            }.decodeSingleOrNull<SocialUser>()
            
            val studyStartTime = currentUser?.studyStartTime ?: 0L
            val currentDuration = currentUser?.studyDuration ?: 0L
            
            val sessionDuration = if (studyStartTime > 0) {
                (System.currentTimeMillis() - studyStartTime) / 1000 // in seconds
            } else 0L
            
            val newTotalDuration = currentDuration + sessionDuration
            
            client.from("users").update(
                {
                    set("status", "IDLE")
                    set("study_start_time", 0L)
                    set("study_duration", newTotalDuration)
                    set("last_active", System.currentTimeMillis())
                }
            ) {
                filter { eq("id", user.id) }
            }
            Log.d(TAG, "Stopped studying. Session: ${sessionDuration}s, Total: ${newTotalDuration}s")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping study", e)
        }
    }

    /**
     * Called when app goes to background - pause studying if active.
     */
    suspend fun onAppBackground() {
        val user = client.auth.currentUserOrNull() ?: return
        
        try {
            val currentUser = getCurrentUserProfile() ?: return
            if (currentUser.status == "STUDYING") {
                stopStudying()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling app background", e)
        }
    }

    /**
     * Called when app resumes from background.
     */
    suspend fun onAppResume() {
        updateHeartbeat()
    }
}

