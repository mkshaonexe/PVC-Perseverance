package com.perseverance.pvc.data

import com.perseverance.pvc.data.SupabaseClient.client
import com.perseverance.pvc.data.model.Group
import com.perseverance.pvc.data.model.Message
import com.perseverance.pvc.data.model.UserGroup
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GroupRepository {

    suspend fun getGroups(): List<Group> {
        return client.from("groups").select().decodeList<Group>()
    }

    suspend fun getUserGroup(userId: String): UserGroup? {
        return try {
            client.from("user_groups")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserGroup>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinGroup(userId: String, groupId: String) {
        val userGroup = UserGroup(userId, groupId, joinedAt = "now()") // Supabase defaults handle ts but good to suffice
        // Logic: specific to schema, if user can validly have multiple entries or just one.
        // Assuming one: Upsert or Insert. Schema constraint assumed unique on user_id?
        // My schema has PK(user_id) so insert/upsert.
        // But serialization expects fields. `joined_at` acts as default on server, but data class makes it required unless default.
        // I will use map/json for insert to avoid date parsing issues on client side insertion if using default.
        
        // Better:
        // client.from("user_groups").upsert(mapOf("user_id" to userId, "group_id" to groupId))
        
        // Using data class needs exact match.
        // I will modify data class slightly to allow optional joinedAt or just send map.
        // Sending map is safer for "default" columns.
        client.from("user_groups").upsert(
            mapOf("user_id" to userId, "group_id" to groupId)
        ) {
            onConflict = "user_id"
        }
    }

    suspend fun getMessages(groupId: String): List<Message> {
        return client.from("messages")
            .select {
                filter {
                    eq("group_id", groupId)
                }
                order("created_at", ascending = true)
            }
            .decodeList<Message>()
    }

    suspend fun sendMessage(userId: String, groupId: String, content: String) {
        val message = mapOf(
            "user_id" to userId,
            "group_id" to groupId,
            "content" to content
        )
        client.from("messages").insert(message)
    }
}
