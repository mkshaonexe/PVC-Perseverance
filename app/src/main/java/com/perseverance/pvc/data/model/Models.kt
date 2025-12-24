package com.perseverance.pvc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String?,
    @SerialName("full_name") val fullName: String?,
    @SerialName("avatar_url") val avatarUrl: String?
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val description: String?,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class UserGroup(
    @SerialName("user_id") val userId: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("joined_at") val joinedAt: String
)

@Serializable
data class Message(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val content: String,
    @SerialName("created_at") val createdAt: String
)
