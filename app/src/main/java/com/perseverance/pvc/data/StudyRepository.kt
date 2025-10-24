package com.perseverance.pvc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_data")

class StudyRepository(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    
    companion object {
        private val STUDY_SESSIONS_KEY = stringPreferencesKey("study_sessions")
        private val SUBJECTS_KEY = stringPreferencesKey("subjects")
        private val TIMER_STATE_KEY = stringPreferencesKey("timer_state")
    }
    
    // Save a completed study session
    suspend fun saveStudySession(session: StudySession) {
        context.dataStore.edit { preferences ->
            val currentSessionsJson = preferences[STUDY_SESSIONS_KEY] ?: "[]"
            val currentSessions: MutableList<StudySession> = gson.fromJson(
                currentSessionsJson,
                object : TypeToken<MutableList<StudySession>>() {}.type
            ) ?: mutableListOf()
            
            currentSessions.add(session)
            preferences[STUDY_SESSIONS_KEY] = gson.toJson(currentSessions)
        }
    }
    
    // Get all study sessions
    fun getAllStudySessions(): Flow<List<StudySession>> {
        return context.dataStore.data.map { preferences ->
            val sessionsJson = preferences[STUDY_SESSIONS_KEY] ?: "[]"
            gson.fromJson(
                sessionsJson,
                object : TypeToken<List<StudySession>>() {}.type
            ) ?: emptyList()
        }
    }
    
    // Get study sessions for a specific date
    fun getStudySessionsByDate(date: LocalDate): Flow<List<StudySession>> {
        return getAllStudySessions().map { sessions ->
            sessions.filter { session ->
                session.startTime?.toLocalDate() == date
            }
        }
    }
    
    // Get total study time for today in seconds (Flow)
    fun getTodayTotalSeconds(): Flow<Int> {
        val today = LocalDate.now()
        return getStudySessionsByDate(today).map { sessions ->
            sessions.sumOf { it.durationSeconds }
        }
    }
    
    // Get total study time for today in seconds (single value, non-blocking)
    suspend fun getTodayTotalSecondsOnce(): Int {
        val today = LocalDate.now()
        val sessionsJson = context.dataStore.data.first()[STUDY_SESSIONS_KEY] ?: "[]"
        val sessions: List<StudySession> = gson.fromJson(
            sessionsJson,
            object : TypeToken<List<StudySession>>() {}.type
        ) ?: emptyList()
        
        return sessions
            .filter { it.startTime?.toLocalDate() == today }
            .sumOf { it.durationSeconds }
    }
    
    // Get study sessions for a date range
    fun getStudySessionsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<StudySession>> {
        return getAllStudySessions().map { sessions ->
            sessions.filter { session ->
                // Add null safety check
                val sessionDate = session.startTime?.toLocalDate() ?: return@filter false
                !sessionDate.isBefore(startDate) && !sessionDate.isAfter(endDate)
            }
        }
    }
    
    // Get total study time by subject (returns minutes)
    fun getTotalTimeBySubject(): Flow<Map<String, Int>> {
        return getAllStudySessions().map { sessions ->
            sessions.groupBy { it.subject }
                .mapValues { (_, sessions) ->
                    // Sum seconds and convert to minutes (floor division)
                    sessions.sumOf { it.durationSeconds } / 60
                }
        }
    }
    
    // Save custom subjects
    suspend fun saveSubjects(subjects: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[SUBJECTS_KEY] = gson.toJson(subjects)
        }
    }
    
    // Get saved subjects
    fun getSavedSubjects(): Flow<List<String>> {
        return context.dataStore.data.map { preferences ->
            val subjectsJson = preferences[SUBJECTS_KEY]
            if (subjectsJson != null) {
                gson.fromJson(
                    subjectsJson,
                    object : TypeToken<List<String>>() {}.type
                ) ?: getDefaultSubjects()
            } else {
                getDefaultSubjects()
            }
        }
    }
    
    // Get study chart data for the last N days
    fun getStudyChartData(days: Int = 7): Flow<StudyChartData> {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong() - 1)
        
        return getStudySessionsInRange(startDate, today).map { sessions ->
            // Filter out sessions with null dates and group by date
            val validSessions = sessions.filter { it.startTime != null }
            val sessionsByDate = validSessions.groupBy { it.startTime.toLocalDate() }
            
            // Create daily data for each day
            val dailyData = (0 until days).map { dayOffset ->
                val date = today.minusDays(dayOffset.toLong())
                val daySessions = sessionsByDate[date] ?: emptyList()
                
                // Group sessions by subject
                val subjectData = daySessions.groupBy { it.subject }
                    .map { (subject, subjectSessions) ->
                        // Convert seconds to minutes (floor division - only count complete minutes)
                        val totalSeconds = subjectSessions.sumOf { it.durationSeconds }
                        SubjectStudyTime(
                            subject = subject,
                            totalMinutes = totalSeconds / 60,  // Floor division
                            sessions = subjectSessions
                        )
                    }
                
                DailyStudyData(
                    date = date,
                    subjects = subjectData
                )
            }.reversed() // Reverse to show oldest to newest
            
            // Calculate total study time in minutes (floor division)
            val totalStudyTime = sessions.sumOf { it.durationSeconds } / 60
            
            // Find most studied subject
            val mostStudiedSubject = sessions
                .groupBy { it.subject }
                .maxByOrNull { (_, sessions) -> sessions.sumOf { it.durationSeconds } }
                ?.key
            
            StudyChartData(
                dailyData = dailyData,
                totalStudyTime = totalStudyTime,
                mostStudiedSubject = mostStudiedSubject
            )
        }
    }
    
    // Clear all data (for testing)
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    private fun getDefaultSubjects(): List<String> {
        return listOf(
            "English",
            "Math",
            "Break"
        )
    }
    
    // Create a new study session
    fun createStudySession(
        subject: String,
        durationSeconds: Int,
        startTime: LocalDateTime = LocalDateTime.now()
    ): StudySession {
        return StudySession(
            id = UUID.randomUUID().toString(),
            subject = subject,
            startTime = startTime,
            endTime = startTime.plusSeconds(durationSeconds.toLong()),
            durationSeconds = durationSeconds
        )
    }
    
    // Save timer state for auto-save functionality
    suspend fun saveTimerState(timerState: PomodoroTimerState?) {
        context.dataStore.edit { preferences ->
            if (timerState != null) {
                preferences[TIMER_STATE_KEY] = gson.toJson(timerState)
            } else {
                preferences.remove(TIMER_STATE_KEY)
            }
        }
    }
    
    // Get weekly study data for a specific week
    fun getWeeklyStudyData(weekStartDate: LocalDate): Flow<WeekStudyData> {
        val weekEndDate = weekStartDate.plusDays(6)
        return getStudySessionsInRange(weekStartDate, weekEndDate).map { sessions ->
            val validSessions = sessions.filter { it.startTime != null }
            val sessionsByDate = validSessions.groupBy { it.startTime!!.toLocalDate() }
            
            // Calculate total study time
            val totalStudyMinutes = validSessions.sumOf { it.durationSeconds } / 60
            val totalStudyHours = totalStudyMinutes / 60.0
            val studyDays = sessionsByDate.keys.size
            val averageStudyMinutesPerDay = if (studyDays > 0) totalStudyMinutes.toDouble() / studyDays else 0.0
            
            // Calculate subject statistics
            val subjectStats = validSessions
                .groupBy { it.subject }
                .map { (subject, subjectSessions) ->
                    val subjectMinutes = subjectSessions.sumOf { it.durationSeconds } / 60
                    val subjectStudyDays = subjectSessions.map { it.startTime!!.toLocalDate() }.distinct().size
                    val subjectAveragePerDay = if (subjectStudyDays > 0) subjectMinutes.toDouble() / subjectStudyDays else 0.0
                    val percentageOfTotal = if (totalStudyMinutes > 0) (subjectMinutes.toDouble() / totalStudyMinutes) * 100 else 0.0
                    
                    SubjectWeeklyStats(
                        subject = subject,
                        totalMinutes = subjectMinutes,
                        averageMinutesPerDay = subjectAveragePerDay,
                        studyDays = subjectStudyDays,
                        percentageOfTotal = percentageOfTotal
                    )
                }
                .sortedByDescending { it.totalMinutes }
            
            // Create daily breakdown
            val dailyBreakdown = (0..6).map { dayOffset ->
                val date = weekStartDate.plusDays(dayOffset.toLong())
                val daySessions = sessionsByDate[date] ?: emptyList()
                
                val subjectData = daySessions.groupBy { it.subject }
                    .map { (subject, subjectSessions) ->
                        val totalSeconds = subjectSessions.sumOf { it.durationSeconds }
                        SubjectStudyTime(
                            subject = subject,
                            totalMinutes = totalSeconds / 60,
                            sessions = subjectSessions
                        )
                    }
                
                DailyStudyData(
                    date = date,
                    subjects = subjectData
                )
            }
            
            WeekStudyData(
                weekStartDate = weekStartDate,
                weekEndDate = weekEndDate,
                totalStudyMinutes = totalStudyMinutes,
                totalStudyHours = totalStudyHours,
                averageStudyMinutesPerDay = averageStudyMinutesPerDay,
                studyDays = studyDays,
                subjects = subjectStats,
                dailyBreakdown = dailyBreakdown
            )
        }
    }
    
    // Get weekly chart data for visualization
    fun getWeeklyChartData(weekStartDate: LocalDate): Flow<WeeklyChartData> {
        return getWeeklyStudyData(weekStartDate).map { weekData ->
            val chartPoints = weekData.dailyBreakdown.map { dailyData ->
                val totalMinutes = dailyData.subjects.sumOf { it.totalMinutes }
                ChartPoint(
                    day = dailyData.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                    totalMinutes = totalMinutes,
                    date = dailyData.date
                )
            }
            
            WeeklyChartData(
                weekData = weekData,
                chartPoints = chartPoints
            )
        }
    }
    
    // Restore timer state
    suspend fun restoreTimerState(): PomodoroTimerState? {
        val timerStateJson = context.dataStore.data.first()[TIMER_STATE_KEY]
        return if (timerStateJson != null) {
            try {
                gson.fromJson(timerStateJson, PomodoroTimerState::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    // Clear timer state
    suspend fun clearTimerState() {
        context.dataStore.edit { preferences ->
            preferences.remove(TIMER_STATE_KEY)
        }
    }
}

