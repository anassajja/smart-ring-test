package com.luffy.smartringtest

import android.content.Context
import androidx.core.content.edit

/**
 * Persists the user's profile (height, weight, age, sex) in SharedPreferences.
 * Used only to remember the setup between app launches.
 */
data class UserProfile(
    val height: Int,
    val weight: Int,
    val age: Int,
    val isMale: Boolean
)

object ProfileStore {

    private const val PREFS_NAME = "user_profile"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_AGE = "age"
    private const val KEY_IS_MALE = "is_male"

    fun load(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val height = prefs.getInt(KEY_HEIGHT, -1)
        val weight = prefs.getInt(KEY_WEIGHT, -1)
        val age = prefs.getInt(KEY_AGE, -1)
        val isMale = prefs.getBoolean(KEY_IS_MALE, true)

        return if (height <= 0 || weight <= 0 || age <= 0) null
        else UserProfile(height, weight, age, isMale)
    }

    fun save(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_HEIGHT, profile.height)
            putInt(KEY_WEIGHT, profile.weight)
            putInt(KEY_AGE, profile.age)
            putBoolean(KEY_IS_MALE, profile.isMale)
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }
}