package com.perseverance.pvc.data

import java.time.LocalDate
import java.time.LocalDateTime

data class StudySession(
    val id: String,
    val subject: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationSeconds: Int  // Store in seconds for accuracy
)

data class SubjectStudyTime(
    val subject: String,
    val totalMinutes: Int,
    val sessions: List<StudySession>
)

data class DailyStudyData(
    val date: LocalDate,
    val subjects: List<SubjectStudyTime>
)

data class StudyChartData(
    val dailyData: List<DailyStudyData>,
    val totalStudyTime: Int,
    val mostStudiedSubject: String?
)

// Timer state for auto-save/restore functionality
data class PomodoroTimerState(
    val sessionStartTime: LocalDateTime?,
    val remainingTimeInSeconds: Int,
    val sessionInitialDuration: Int,
    val selectedSubject: String,
    val sessionType: String, // "WORK", "SHORT_BREAK", "LONG_BREAK"
    val completedSessions: Int,
    val isPlaying: Boolean,
    val isPaused: Boolean
)