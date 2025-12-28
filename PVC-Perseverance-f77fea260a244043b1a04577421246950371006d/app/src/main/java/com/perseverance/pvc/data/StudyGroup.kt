package com.perseverance.pvc.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null
)
