package com.perseverance.pvc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile_data")

class ProfileRepository(private val context: Context) {

    companion object {
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        private val PHOTO_URL_KEY = stringPreferencesKey("photo_url")
        private val BIO_KEY = stringPreferencesKey("bio")
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val DATE_OF_BIRTH_KEY = stringPreferencesKey("date_of_birth")
        private val ADDRESS_KEY = stringPreferencesKey("address")
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
        private val SECONDARY_EMAIL_KEY = stringPreferencesKey("secondary_email")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    suspend fun saveProfile(
        displayName: String,
        photoUrl: String,
        bio: String,
        gender: String,
        dateOfBirth: String,
        address: String,
        phoneNumber: String,
        secondaryEmail: String,
        username: String
    ) {
        context.profileDataStore.edit { preferences ->
            preferences[DISPLAY_NAME_KEY] = displayName
            preferences[PHOTO_URL_KEY] = photoUrl
            preferences[BIO_KEY] = bio
            preferences[GENDER_KEY] = gender
            preferences[DATE_OF_BIRTH_KEY] = dateOfBirth
            preferences[ADDRESS_KEY] = address
            preferences[PHONE_NUMBER_KEY] = phoneNumber
            preferences[SECONDARY_EMAIL_KEY] = secondaryEmail
            preferences[USERNAME_KEY] = username
        }
    }

    fun getProfile(): Flow<SocialUser> {
        return context.profileDataStore.data.map { preferences ->
            SocialUser(
                displayName = preferences[DISPLAY_NAME_KEY] ?: "",
                photoUrl = preferences[PHOTO_URL_KEY] ?: "",
                bio = preferences[BIO_KEY] ?: "",
                gender = preferences[GENDER_KEY] ?: "",
                dateOfBirth = preferences[DATE_OF_BIRTH_KEY] ?: "",
                address = preferences[ADDRESS_KEY] ?: "",
                phoneNumber = preferences[PHONE_NUMBER_KEY] ?: "",
                secondaryEmail = preferences[SECONDARY_EMAIL_KEY] ?: "",
                username = preferences[USERNAME_KEY] ?: ""
            )
        }
    }
}
