package com.perseverance.pvc.data

import java.time.LocalDate
import java.time.LocalDateTime

data class StudySession(
    val id: String,
    val subject: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationMinutes: Int
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
