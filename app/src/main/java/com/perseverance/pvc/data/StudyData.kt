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
    val isPaused: Boolean,
    val lastUpdateTime: LocalDateTime = LocalDateTime.now() // Track when timer was last active
)

// Weekly study data models
data class SubjectWeeklyStats(
    val subject: String,
    val totalMinutes: Int,
    val averageMinutesPerDay: Double,
    val studyDays: Int,
    val percentageOfTotal: Double
)

data class WeekStudyData(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val totalStudyMinutes: Int,
    val totalStudyHours: Double,
    val averageStudyMinutesPerDay: Double,
    val studyDays: Int,
    val subjects: List<SubjectWeeklyStats>,
    val dailyBreakdown: List<DailyStudyData>
)

data class WeeklyChartData(
    val weekData: WeekStudyData,
    val chartPoints: List<ChartPoint> // For line/bar chart visualization
)

data class ChartPoint(
    val day: String,
    val totalMinutes: Int,
    val date: LocalDate
)