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
}
