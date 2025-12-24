package com.perseverance.pvc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

private val Context.missionDataStore: DataStore<Preferences> by preferencesDataStore(name = "mission_data")

class MissionRepository(private val context: Context, private val studyRepository: StudyRepository) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    companion object {
        private val JOINED_GLOBAL_MISSION_KEY = booleanPreferencesKey("joined_global_mission_101")
        private val CUSTOM_MISSIONS_KEY = stringPreferencesKey("custom_missions")
        
        // Constants for the 101 Hours Challenge
        const val GLOBAL_MISSION_ID = "global_101_hours_2025"
        const val GLOBAL_MISSION_TARGET_HOURS = 101
        const val GLOBAL_MISSION_TARGET_SECONDS = GLOBAL_MISSION_TARGET_HOURS * 3600L
    }

    // Get the Global "101 Hours Challenge" Mission
    fun getGlobalMission(): Flow<Mission> {
        return context.missionDataStore.data.map { preferences ->
            val isJoined = preferences[JOINED_GLOBAL_MISSION_KEY] ?: false
            
            // Calculate progress if joined
            // For now, let's look at study time for the last 7 days as the challenge window
            // Or strictly from Dec 25th 2025 if we were in that timeframe.
            // Requirement mentions "101 hours in 168 hours (last week of 2025)".
            // We will use a rolling 7-day window for the "offline" simulation feel,
            // or specific dates if the user strictly wants "Last week of 2025".
            // Let's implement Rolling 7 Days for immediate feedback utility.
            
            Mission(
                id = GLOBAL_MISSION_ID,
                title = "101 Hours Challenge",
                description = "Study 101 hours in the last week of 2025 (168 Hours). The toughest mission!",
                targetSeconds = GLOBAL_MISSION_TARGET_SECONDS,
                deadline = LocalDateTime.of(2025, 12, 31, 23, 59),
                isJoined = isJoined,
                isGlobal = true,
                participantCount = 1240 // Mocked participant count
            )
        }
    }

    // Join the Global Mission
    suspend fun joinGlobalMission() {
        context.missionDataStore.edit { preferences ->
            preferences[JOINED_GLOBAL_MISSION_KEY] = true
        }
    }
    
    // Get custom missions (User created)
    fun getCustomMissions(): Flow<List<Mission>> {
        return context.missionDataStore.data.map { preferences ->
            val validJson = preferences[CUSTOM_MISSIONS_KEY] ?: "[]"
             try {
                gson.fromJson(validJson, object : TypeToken<List<Mission>>() {}.type)
            } catch (e: Exception) {
                emptyList<Mission>()
            }
        }
    }

    // Add a custom mission
    suspend fun addCustomMission(title: String, targetHours: Int, deadline: LocalDateTime?) {
         context.missionDataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_MISSIONS_KEY] ?: "[]"
            val currentList: MutableList<Mission> = try {
                 gson.fromJson(currentJson, object : TypeToken<MutableList<Mission>>() {}.type)
            } catch (e: Exception) {
                mutableListOf()
            }
            
            val newMission = Mission(
                id = UUID.randomUUID().toString(),
                title = title,
                description = "Custom Mission",
                targetSeconds = targetHours * 3600L,
                deadline = deadline,
                isJoined = true, // Auto-join custom missions
                isGlobal = false,
                participantCount = 1 // Just you
            )
            
            currentList.add(newMission)
            preferences[CUSTOM_MISSIONS_KEY] = gson.toJson(currentList)
        }
    }

    // Helper to calculate progress for missions
    // This connects to StudyRepository to get actual study time
    fun getMissionProgress(mission: Mission): Flow<Long> {
        // If it's the 101 hours challenge, we track "Last 7 Days" or specific range
        if (mission.id == GLOBAL_MISSION_ID) {
            // "Last Week of 2025" logic or Rolling 7 days.
            // Let's stick to Rolling 7 Days as it's more interactive for testing now.
            // Or ideally, "This Week".
            val now = LocalDateTime.now()
            val start = now.minusDays(7)
            return studyRepository.getTotalStudyTimeInWindow(start, now)
        } else {
             // For custom missions, we might track "All time since creation" or "This week"
             // Simplified: Track all study time for now as a base, or we'd need 'createdDate' in Mission.
             // Let's assume Custom Missions track "Duration since they were added".
             // We'll need to store 'startDate' in Mission to do this properly.
             // Updating Mission model briefly implicitly in next steps if needed.
             // For now, let's return a simple specific query.
             
             // Placeholder: Return 0 until we have better "Start Date" tracking for custom missions
             return studyRepository.getTodayTotalSeconds().map { it.toLong() } // Just today's time for now as placeholder
        }
    }
}
