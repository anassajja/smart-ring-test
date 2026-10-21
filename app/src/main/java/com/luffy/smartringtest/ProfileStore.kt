package com.luffy.smartringtest

import android.content.Context
import androidx.core.content.edit

/**
 * Persists the user's profile (email, height, weight, age, sex, avatar) in SharedPreferences.
 */
data class UserProfile(
    val email: String = "user@smartring.com",
    val height: Int = 175,
    val weight: Int = 70,
    val age: Int = 25,
    val isMale: Boolean = true,
    val avatarUri: String? = null,
    val avatarPreset: String = ""
)

object ProfileStore {

    private const val PREFS_NAME = "user_profile"
    private const val KEY_EMAIL = "email"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_AGE = "age"
    private const val KEY_IS_MALE = "is_male"
    private const val KEY_AVATAR_URI = "avatar_uri"
    private const val KEY_AVATAR_PRESET = "avatar_preset"
    private const val KEY_STEP_GOAL = "step_goal"

    fun load(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_EMAIL, "user@smartring.com") ?: "user@smartring.com"
        val height = prefs.getInt(KEY_HEIGHT, 175)
        val weight = prefs.getInt(KEY_WEIGHT, 70)
        val age = prefs.getInt(KEY_AGE, 25)
        val isMale = prefs.getBoolean(KEY_IS_MALE, true)
        val avatarUri = prefs.getString(KEY_AVATAR_URI, null)
        val avatarPreset = prefs.getString(KEY_AVATAR_PRESET, "") ?: ""

        return UserProfile(email, height, weight, age, isMale, avatarUri, avatarPreset)
    }

    fun loadStepGoal(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_STEP_GOAL, 10000)
    }

    fun saveStepGoal(context: Context, goal: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putInt(KEY_STEP_GOAL, goal)
        }
    }

    fun save(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_EMAIL, profile.email)
            putInt(KEY_HEIGHT, profile.height)
            putInt(KEY_WEIGHT, profile.weight)
            putInt(KEY_AGE, profile.age)
            putBoolean(KEY_IS_MALE, profile.isMale)
            putString(KEY_AVATAR_URI, profile.avatarUri)
            putString(KEY_AVATAR_PRESET, profile.avatarPreset)
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }
}
