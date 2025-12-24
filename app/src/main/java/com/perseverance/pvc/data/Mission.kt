package com.perseverance.pvc.data

import java.time.LocalDateTime

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val targetSeconds: Long,
    val progressSeconds: Long = 0,
    val deadline: LocalDateTime? = null,
    val isJoined: Boolean = false,
    val isGlobal: Boolean = false, // True for the 101 Hours Challenge
    val participantCount: Int = 0 // Mocked for global missions
)
