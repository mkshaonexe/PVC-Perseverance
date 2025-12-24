package com.perseverance.pvc.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    
    private val TAG = "SocialRepository"

    // --- User Management ---

    // Create or update user profile upon login
    suspend fun updateCurrentUserProfile() {
        val user = auth.currentUser ?: return
        
        val userData = hashMapOf(
            "uid" to user.uid,
            "displayName" to (user.displayName ?: "Unknown"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "lastActive" to System.currentTimeMillis()
        )
        
        try {
            db.collection("users").document(user.uid)
                .set(userData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
        }
    }

    // --- Status Signaling (The "P2P" Logic) ---

    // Call this when Timer Starts
    suspend fun setStatusStudying(subject: String, durationSeconds: Long) {
        val user = auth.currentUser ?: return
        
        val updates = hashMapOf(
            "status" to "STUDYING",
            "currentSubject" to subject,
            "studyStartTime" to System.currentTimeMillis(),
            "studyDuration" to durationSeconds
        )
        
        try {
            db.collection("users").document(user.uid)
                .update(updates as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // Call this when Timer Stops
    suspend fun setStatusIdle() {
        val user = auth.currentUser ?: return
        
        val updates = hashMapOf(
            "status" to "IDLE",
            "studyStartTime" to 0
        )
        
        try {
            db.collection("users").document(user.uid)
                .update(updates as Map<String, Any>)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting status", e)
        }
    }

    // --- Friend System (10 Friend Limit) ---

    // Send Friend Request (Check limit first)
    suspend fun sendFriendRequest(email: String): Result<String> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        
        // 1. Find user by email
        val snapshot = db.collection("users")
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
        val myFriends = db.collection("users").document(currentUser.uid)
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
        
        db.collection("users").document(targetUid)
            .collection("friendRequests")
            .document(currentUser.uid)
            .set(request)
            .await()
            
        return Result.success("Request sent to ${targetUser.getString("displayName")}")
    }
    
    // Accept Friend Request
    suspend fun acceptFriendRequest(requestFromUid: String, requestFromName: String) {
        val currentUser = auth.currentUser ?: return
        
        // Add to MY friends
        val myFriendData = hashMapOf(
            "uid" to requestFromUid,
            "displayName" to requestFromName,
            "since" to System.currentTimeMillis()
        )
        
        db.collection("users").document(currentUser.uid)
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
        
        db.collection("users").document(requestFromUid)
            .collection("friends")
            .document(currentUser.uid)
            .set(theirFriendData)
            .await()
            
        // Delete request
        db.collection("users").document(currentUser.uid)
            .collection("friendRequests")
            .document(requestFromUid)
            .delete()
            .await()
    }

    // --- Real-time Listeners ---

    // Listen to my friends' status
    fun getFriendsStatusWaitList(): Flow<List<SocialUser>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close()
            return@callbackFlow
        }
        
        // 1. Get List of Friend UIDs
        val friendsSnapshot = db.collection("users").document(currentUser.uid)
            .collection("friends")
            .get()
            .await()
            
        val friendIds = friendsSnapshot.documents.map { it.id }
        
        if (friendIds.isEmpty()) {
            trySend(emptyList())
            // Keep flow open but with empty list
            awaitClose { }
            return@callbackFlow
        }
        
        // 2. Query users where UID is in my friend list
        // Firestore 'in' query supports up to 10 items, perfect for our limit!
        val subscription = db.collection("users")
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
    }
}
