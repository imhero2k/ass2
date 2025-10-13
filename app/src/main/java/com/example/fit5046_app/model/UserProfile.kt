package com.example.fit5046_app.model

import java.util.Date

data class UserProfile(
    val userId: String = "",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val healthInfo: HealthInfo = HealthInfo(),
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class PersonalInfo(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.OTHER,
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val postalCode: String = "",
    val profileImageUrl: String = ""
)

data class HealthInfo(
    val height: String = "",
    val weight: String = "",
    val bloodType: BloodType = BloodType.UNKNOWN,
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val healthConditions: List<HealthCondition> = emptyList(),
    val emergencyContact: EmergencyContact = EmergencyContact()
)

data class UserPreferences(
    val units: MeasurementUnits = MeasurementUnits.METRIC,
    val language: String = "English",
    val notifications: NotificationSettings = NotificationSettings(),
    val privacy: PrivacySettings = PrivacySettings()
)

data class EmergencyContact(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)


data class NotificationSettings(
    val medicationReminders: Boolean = true,
    val exerciseReminders: Boolean = true,
    val mealReminders: Boolean = true,
    val healthCheckReminders: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true
)

data class PrivacySettings(
    val shareHealthData: Boolean = false,
    val shareWithFamily: Boolean = false,
    val shareWithDoctor: Boolean = false,
    val dataRetention: Int = 365 // days
)

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum class BloodType {
    A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
    AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE, UNKNOWN
}

enum class HealthCondition {
    DIABETES, HYPERTENSION, HEART_DISEASE, ASTHMA,
    ARTHRITIS, DEPRESSION, ANXIETY, MIGRAINE,
    HIGH_CHOLESTEROL, OBESITY, NONE
}

enum class MeasurementUnits {
    METRIC, IMPERIAL
}

enum class Relationship {
    SPOUSE, PARENT, CHILD, SIBLING, FRIEND,
    CAREGIVER, OTHER
}
