package com.perseverance.pvc.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    // Make Firebase instances nullable and safe catch initialization
    private val db = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    
    private val TAG = "SocialRepository"

    // --- User Management ---

    // Create or update user profile upon login
    suspend fun updateCurrentUserProfile() {
        val user = auth?.currentUser ?: return
        val database = db ?: return
        
        val userData = hashMapOf(
            "uid" to user.uid,
            "displayName" to (user.displayName ?: "Unknown"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "lastActive" to System.currentTimeMillis()
        )
        
        try {
            database.collection("users").document(user.uid)
                .set(userData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
        }
    }

    // --- Status Signaling (The "P2P" Logic) ---

    // Call this when Timer Starts
    suspend fun setStatusStudying(subject: String, durationSeconds: Long) {
        val user = auth?.currentUser ?: return
        val database = db ?: return
        
        val updates = hashMapOf(
            "status" to "STUDYING",
            "currentSubject" to subject,
            "studyStartTime" to System.currentTimeMillis(),
            "studyDuration" to durationSeconds
        )
        
        try {
            database.collection("users").document(user.uid)
                .update(updates as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // Call this when Timer Stops
    suspend fun setStatusIdle() {
        val user = auth?.currentUser ?: return
        val database = db ?: return
        
        val updates = hashMapOf(
            "status" to "IDLE",
            "studyStartTime" to 0
        )
        
        try {
            database.collection("users").document(user.uid)
                .update(updates as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // --- Friend System (10 Friend Limit) ---

    // Send Friend Request (Check limit first)
    suspend fun sendFriendRequest(email: String): Result<String> {
        val currentUser = auth?.currentUser ?: return Result.failure(Exception("Not logged in (Offline Mode)"))
        val database = db ?: return Result.failure(Exception("Database unavailable (Offline Mode)"))
        
        try {
            // 1. Find user by email
            val snapshot = database.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
                
            if (snapshot.isEmpty) {
                return Result.failure(Exception("User not found"))
            }
            
            val targetUser = snapshot.documents[0]
            val targetUid = targetUser.getString("uid") ?: return Result.failure(Exception("Invalid user"))

            if (targetUid == currentUser.uid) {
                return Result.failure(Exception("Cannot add yourself"))
            }

            // 2. Check my friend count
            val myFriends = database.collection("users").document(currentUser.uid)
                .collection("friends")
                .get()
                .await()
                
            if (myFriends.size() >= 10) {
                return Result.failure(Exception("You have reached the 10 friend limit."))
            }

            // 3. Send Request
            val request = hashMapOf(
                "fromUid" to currentUser.uid,
                "fromName" to (currentUser.displayName ?: "Unknown"),
                "status" to "PENDING"
            )
            
            database.collection("users").document(targetUid)
                .collection("friendRequests")
                .document(currentUser.uid)
                .set(request)
                .await()
                
            return Result.success("Request sent to ${targetUser.getString("displayName")}")
        } catch (e: Exception) {
             return Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Accept Friend Request
    suspend fun acceptFriendRequest(requestFromUid: String, requestFromName: String) {
        val currentUser = auth?.currentUser ?: return
        val database = db ?: return
        
        try {
            // Add to MY friends
            val myFriendData = hashMapOf(
                "uid" to requestFromUid,
                "displayName" to requestFromName,
                "since" to System.currentTimeMillis()
            )
            
            database.collection("users").document(currentUser.uid)
                .collection("friends")
                .document(requestFromUid)
                .set(myFriendData)
                .await()
                
            // Add ME to THEIR friends
            val theirFriendData = hashMapOf(
                "uid" to currentUser.uid,
                "displayName" to (currentUser.displayName ?: "Unknown"),
                "since" to System.currentTimeMillis()
            )
            
            database.collection("users").document(requestFromUid)
                .collection("friends")
                .document(currentUser.uid)
                .set(theirFriendData)
                .await()
                
            // Delete request
            database.collection("users").document(currentUser.uid)
                .collection("friendRequests")
                .document(requestFromUid)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request", e)
        }
    }

    // --- Real-time Listeners ---

    // Listen to my friends' status
    fun getFriendsStatusWaitList(): Flow<List<SocialUser>> = callbackFlow {
        val currentUser = auth?.currentUser
        val database = db
        
        if (currentUser == null || database == null) {
            trySend(emptyList()) // Cleanly return empty list in offline mode
            // Keep flow open to avoid immediate completion if downstream expects stream
            awaitClose { } 
            return@callbackFlow
        }
        
        try {
            // 1. Get List of Friend UIDs
            val friendsSnapshot = database.collection("users").document(currentUser.uid)
                .collection("friends")
                .get()
                .await()
                
            val friendIds = friendsSnapshot.documents.map { it.id }
            
            if (friendIds.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            
            // 2. Query users where UID is in my friend list
            // Firestore 'in' query supports up to 10 items, perfect for our limit!
            val subscription = database.collection("users")
                .whereIn("uid", friendIds)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val friends = snapshot.toObjects(SocialUser::class.java)
                        trySend(friends)
                    }
                }
                
            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting friends status", e)
            trySend(emptyList())
            awaitClose { }
        }
    }
}
