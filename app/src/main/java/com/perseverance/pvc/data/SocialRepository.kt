package com.perseverance.pvc.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocialUser(
    @SerialName("id")
    val id: String = "",
    @SerialName("firebase_uid")
    val uid: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("photo_url")
    val photoUrl: String = "",
    @SerialName("status")
    val status: String = "IDLE", // IDLE, STUDYING
    @SerialName("current_subject")
    val currentSubject: String = "",
    @SerialName("last_active")
    val lastActive: String = "",
    val studyStartTime: Long = 0,
    val studyDuration: Long = 0
)

@Serializable
data class FriendRequest(
    @SerialName("id")
    val id: String = "",
    @SerialName("from_user_id")
    val fromUserId: String = "",
    @SerialName("to_user_email")
    val toUserEmail: String = "",
    @SerialName("status")
    val status: String = "PENDING" // PENDING, ACCEPTED, REJECTED
)

@Serializable
data class Friendship(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("friend_id")
    val friendId: String = ""
)

class SocialRepository {
    private val supabase = SupabaseClient.client
    private val firebaseAuth = Firebase.auth
    
    private val TAG = "SocialRepository"

    // --- User Management ---

    // Create or update user profile upon login
    suspend fun updateCurrentUserProfile(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(Exception("Not logged in"))
            val supabaseUser = supabase.auth.currentUserOrNull() ?: return Result.failure(Exception("Not signed in to Supabase"))
            
            // Check if user exists
            val existingUsers = supabase.from("users")
                .select(Columns.list("id")) {
                    filter {
                        eq("firebase_uid", firebaseUser.uid)
                    }
                }
                .decodeList<Map<String, String>>()
            
            if (existingUsers.isEmpty()) {
                // Insert new user
                supabase.from("users").insert(
                    mapOf(
                        "firebase_uid" to firebaseUser.uid,
                        "email" to (firebaseUser.email ?: ""),
                        "display_name" to (firebaseUser.displayName ?: "User"),
                        "photo_url" to (firebaseUser.photoUrl?.toString() ?: ""),
                        "status" to "IDLE"
                    )
                )
                Log.d(TAG, "Created new user profile in Supabase")
            } else {
                // Update existing user
                supabase.from("users").update(
                    mapOf(
                        "display_name" to (firebaseUser.displayName ?: "User"),
                        "photo_url" to (firebaseUser.photoUrl?.toString() ?: ""),
                        "last_active" to "NOW()"
                    )
                ) {
                    filter {
                        eq("firebase_uid", firebaseUser.uid)
                    }
                }
                Log.d(TAG, "Updated user profile in Supabase")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }

    // --- Status Signaling ---

    // Call this when Timer Starts
    suspend fun setStatusStudying(subject: String, durationSeconds: Long): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(Exception("Not logged in"))
            
            supabase.from("users").update(
                mapOf(
                    "status" to "STUDYING",
                    "current_subject" to subject
                )
            ) {
                filter {
                    eq("firebase_uid", firebaseUser.uid)
                }
            }
            
            Log.d(TAG, "Status set to STUDYING")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
            Result.failure(e)
        }
    }

    // Call this when Timer Stops
    suspend fun setStatusIdle(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(Exception("Not logged in"))
            
            supabase.from("users").update(
                mapOf(
                    "status" to "IDLE",
                    "current_subject" to ""
                )
            ) {
                filter {
                    eq("firebase_uid", firebaseUser.uid)
                }
            }
            
            Log.d(TAG, "Status set to IDLE")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
            Result.failure(e)
        }
    }

    // --- Friend System (10 Friend Limit) ---

    // Send Friend Request (Check limit first)
    suspend fun sendFriendRequest(email: String): Result<String> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(Exception("Not logged in"))
            
            // Get current user's Supabase ID
            val currentUserData = supabase.from("users")
                .select(Columns.list("id")) {
                    filter {
                        eq("firebase_uid", firebaseUser.uid)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>() ?: return Result.failure(Exception("User not found"))
            
            val currentUserId = currentUserData["id"] ?: return Result.failure(Exception("Invalid user"))
            
            // Check if target user exists
            val targetUser = supabase.from("users")
                .select(Columns.list("id", "email", "display_name")) {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()
            
            if (targetUser == null) {
                return Result.failure(Exception("User with email $email not found"))
            }
            
            if (targetUser["email"] == firebaseUser.email) {
                return Result.failure(Exception("Cannot add yourself"))
            }
            
            // Check friend count
            val friendCount = supabase.from("friends")
                .select(Columns.list("id")) {
                    filter {
                        eq("user_id", currentUserId)
                    }
                }
                .decodeList<Map<String, String>>()
                .size
            
            if (friendCount >= 10) {
                return Result.failure(Exception("You have reached the 10 friend limit"))
            }
            
            // Check if request already exists
            val existingRequest = supabase.from("friend_requests")
                .select(Columns.list("id")) {
                    filter {
                        eq("from_user_id", currentUserId)
                        eq("to_user_email", email)
                    }
                }
                .decodeList<Map<String, String>>()
            
            if (existingRequest.isNotEmpty()) {
                return Result.failure(Exception("Friend request already sent"))
            }
            
            // Send request
            supabase.from("friend_requests").insert(
                mapOf(
                    "from_user_id" to currentUserId,
                    "to_user_email" to email,
                    "status" to "PENDING"
                )
            )
            
            Result.success("Request sent to ${targetUser["display_name"]}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending friend request", e)
            Result.failure(e)
        }
    }

    // Get Friends Status with periodic updates
    fun getFriendsStatusWaitList(): Flow<List<SocialUser>> = kotlinx.coroutines.flow.flow {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            emit(emptyList())
            return@flow
        }
        
        // Emit initial friends list
        emit(getFriendsList(firebaseUser.uid))
        
        // Poll for updates every 10 seconds
        while (true) {
            kotlinx.coroutines.delay(10000)
            try {
                emit(getFriendsList(firebaseUser.uid))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching friends update", e)
            }
        }
    }
    
    // Helper function to get friends list
    private suspend fun getFriendsList(firebaseUid: String): List<SocialUser> {
        return try {
            // Get current user's Supabase ID
            val currentUserData = supabase.from("users")
                .select(Columns.list("id")) {
                    filter {
                        eq("firebase_uid", firebaseUid)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>() ?: return emptyList()
            
            val currentUserId = currentUserData["id"] ?: return emptyList()
            
            // Get friend IDs
            val friendships = supabase.from("friends")
                .select(Columns.list("friend_id")) {
                    filter {
                        eq("user_id", currentUserId)
                    }
                }
                .decodeList<Map<String, String>>()
            
            if (friendships.isEmpty()) {
                return emptyList()
            }
            
            val friendIds = friendships.mapNotNull { it["friend_id"] }
            
            // Get friend profiles
            val friends = supabase.from("users")
                .select() {
                    filter {
                        isIn("id", friendIds)
                    }
                }
                .decodeList<SocialUser>()
            
            friends
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching friends list", e)
            emptyList()
        }
    }
}
