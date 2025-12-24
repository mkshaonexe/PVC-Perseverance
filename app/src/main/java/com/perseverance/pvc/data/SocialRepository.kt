package com.perseverance.pvc.data

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable

data class SocialUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val status: String = "IDLE", // IDLE, STUDYING
    val currentSubject: String = "",
    val lastActive: Long = 0,
    val studyStartTime: Long = 0,
    val studyDuration: Long = 0
)

data class FriendRequest(
    val fromUid: String = "",
    val fromName: String = "",
    val status: String = "PENDING" // PENDING, ACCEPTED
)

class SocialRepository {
    private val supabase = com.perseverance.pvc.data.remote.SupabaseClient.client
    
    private val TAG = "SocialRepository"

    // --- User Management ---

    // Create or update user profile upon login
    suspend fun updateCurrentUserProfile() {
        val user = supabase.auth.currentUserOrNull() ?: return
        
        val userData = mapOf(
            "id" to user.id,
            "display_name" to (user.userMetadata?.get("full_name")?.toString() ?: "Unknown"),
            "email" to (user.email ?: ""),
            "photo_url" to (user.userMetadata?.get("avatar_url")?.toString() ?: ""),
            "last_active" to System.currentTimeMillis()
        )
        
        try {
            supabase.postgrest.from("profiles").upsert(userData)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
        }
    }

    // --- Status Signaling ---

    // Call this when Timer Starts
    suspend fun setStatusStudying(subject: String, durationSeconds: Long) {
        val user = supabase.auth.currentUserOrNull() ?: return
        
        val updates = mapOf(
            "status" to "STUDYING",
            "current_subject" to subject,
            "study_start_time" to System.currentTimeMillis(),
            "study_duration" to durationSeconds
        )
        
        try {
            supabase.postgrest.from("profiles").update(updates) {
                filter {
                    eq("id", user.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // Call this when Timer Stops
    suspend fun setStatusIdle() {
        val user = supabase.auth.currentUserOrNull() ?: return
        
        val updates = mapOf(
            "status" to "IDLE",
            "study_start_time" to 0
        )
        
        try {
            supabase.postgrest.from("profiles").update(updates) {
                filter {
                    eq("id", user.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // --- Friend System ---

    // Send Friend Request
    suspend fun sendFriendRequest(email: String): Result<String> {
        val currentUser = supabase.auth.currentUserOrNull() ?: return Result.failure(Exception("Not logged in"))
        
        try {
            // 1. Find user by email
            val users = supabase.postgrest.from("profiles").select {
                filter {
                    eq("email", email)
                }
            }.decodeList<SocialProfile>()
            
            if (users.isEmpty()) {
                return Result.failure(Exception("User not found"))
            }
            
            val targetUser = users[0]
            val targetUid = targetUser.id
            
            if (targetUid == currentUser.id) {
                return Result.failure(Exception("Cannot add yourself"))
            }

            // 2. Check my friend count (simplified check)
            val friends = supabase.postgrest.from("friends").select {
                filter {
                    eq("user_id", currentUser.id)
                }
            }.decodeList<FriendRelation>().size
            
            if (friends >= 10) {
                return Result.failure(Exception("You have reached the 10 friend limit."))
            }

            // 3. Send Request
            val request = mapOf(
                "from_uid" to currentUser.id,
                "to_uid" to targetUid,
                "status" to "PENDING"
            )
            
            supabase.postgrest.from("friend_requests").insert(request)
                
            return Result.success("Request sent to ${targetUser.displayName}")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    // Accept Friend Request (Not implemented in UI yet but good to have)
    suspend fun acceptFriendRequest(requestId: String) {
         // TODO: Implement
    }

    // --- Real-time Listeners ---

    // Poll friend status (Realtime is better but Polling is easier to start with Postgrest)
    // Supabase Realtime requires enabling replication on tables.
    fun getFriendsStatusWaitList(): Flow<List<SocialUser>> = callbackFlow {
        val currentUser = supabase.auth.currentUserOrNull()
        if (currentUser == null) {
            close()
            return@callbackFlow
        }
        
        // Polling loop
        while(true) {
            try {
                // 1. Get List of Friend UIDs
                // Assuming 'friends' table: user_id, friend_id
                val friendsRelations = supabase.postgrest.from("friends").select {
                    filter {
                        eq("user_id", currentUser.id)
                    }
                }.decodeList<FriendRelation>()
                
                val friendIds = friendsRelations.map { it.friendId }
                
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    // 2. Query profiles
                    // Using filter with 'in'
                    val friendProfiles = supabase.postgrest.from("profiles").select {
                        filter {
                            isIn("id", friendIds)
                        }
                    }.decodeList<SocialProfile>()
                    
                    // Map back to SocialUser for UI
                    val socialUsers = friendProfiles.map { profile ->
                        SocialUser(
                            uid = profile.id,
                            displayName = profile.displayName,
                            email = profile.email,
                            photoUrl = profile.photoUrl,
                            status = profile.status,
                            currentSubject = profile.currentSubject,
                            lastActive = profile.lastActive,
                            studyStartTime = profile.studyStartTime,
                            studyDuration = profile.studyDuration
                        )
                    }
                    trySend(socialUsers)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Polling error", e)
            }
            
            // Poll every 10 seconds
            kotlinx.coroutines.delay(10000)
        }
    }
}

@kotlinx.serialization.Serializable
data class SocialProfile(
    val id: String,
    @kotlinx.serialization.SerialName("display_name") val displayName: String = "",
    val email: String = "",
    @kotlinx.serialization.SerialName("photo_url") val photoUrl: String = "",
    val status: String = "IDLE",
    @kotlinx.serialization.SerialName("current_subject") val currentSubject: String = "",
    @kotlinx.serialization.SerialName("last_active") val lastActive: Long = 0,
    @kotlinx.serialization.SerialName("study_start_time") val studyStartTime: Long = 0,
    @kotlinx.serialization.SerialName("study_duration") val studyDuration: Long = 0
)

@kotlinx.serialization.Serializable
data class FriendRelation(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("friend_id") val friendId: String
)
