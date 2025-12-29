package com.perseverance.pvc.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user's membership in a study group.
 * Maps to the `group_members` table in Supabase.
 */
@Serializable
data class GroupMemberRecord(
    val id: String = "",
    @SerialName("group_id") val groupId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("joined_at") val joinedAt: String = ""
)

/**
 * Represents a group member with their full user info and study status.
 * Used for UI display in group details screen.
 */
@Serializable
data class GroupMemberWithStatus(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String?,
    val status: String = "IDLE", // STUDYING or IDLE
    @SerialName("study_start_time") val studyStartTime: Long = 0, // Unix timestamp when study started
    @SerialName("study_duration") val studyDuration: Long = 0, // Total accumulated study time in seconds
    @SerialName("last_active") val lastActive: Long = 0, // Last heartbeat timestamp
    @SerialName("current_subject") val currentSubject: String = ""
) {
    val isStudying: Boolean get() = status == "STUDYING"
    
    /**
     * Calculate the current live study time.
     * If studying, includes time since studyStartTime.
     */
    fun getLiveStudySeconds(): Long {
        return if (isStudying && studyStartTime > 0) {
            val elapsedSinceStart = (System.currentTimeMillis() - studyStartTime) / 1000
            studyDuration + elapsedSinceStart
        } else {
            studyDuration
        }
    }
    
    /**
     * Check if user is considered online (active within last 2 minutes).
     */
    fun isOnline(): Boolean {
        val twoMinutesAgo = System.currentTimeMillis() - (2 * 60 * 1000)
        return lastActive > twoMinutesAgo
    }
    
    /**
     * Format study time as HH:MM:SS string.
     */
    fun getFormattedTime(liveSeconds: Long = getLiveStudySeconds()): String {
        val hours = liveSeconds / 3600
        val minutes = (liveSeconds % 3600) / 60
        val seconds = liveSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
