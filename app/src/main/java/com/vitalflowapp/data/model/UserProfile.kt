package com.vitalflowapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateOfBirth: String? = null,
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val avatarColor: Int = 0,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val targetSystolicMin: Int = 90,
    val targetSystolicMax: Int = 120,
    val targetDiastolicMin: Int = 60,
    val targetDiastolicMax: Int = 80,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val enableCrisisAlerts: Boolean = true
) : Parcelable

enum class Gender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}
