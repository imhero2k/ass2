package com.example.fit5046_app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class UserProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class UserProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storageService = FirebaseStorageService()

    private val _profileState = MutableStateFlow(UserProfileState())
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)

            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val document = firestore.collection("userProfiles").document(userId).get().await()
                    if (document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            profile = profile
                        )
                    } else {
                        // Create new profile if doesn't exist
                        val newProfile = UserProfile(
                            userId = userId,
                            personalInfo = com.example.fit5046_app.model.PersonalInfo(
                                email = auth.currentUser?.email ?: ""
                            )
                        )
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            profile = newProfile
                        )
                    }
                } else {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)

            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val updatedProfile = profile.copy(
                        userId = userId,
                        updatedAt = Date()
                    )

                    firestore.collection("userProfiles")
                        .document(userId)
                        .set(updatedProfile)
                        .await()

                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        profile = updatedProfile,
                        isSaved = true
                    )
                } else {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        errorMessage = "User not authenticated"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to save profile"
                )
            }
        }
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(errorMessage = null)
    }

    fun clearSaveStatus() {
        _profileState.value = _profileState.value.copy(isSaved = false)
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)

                // Upload image to Firebase Storage
                val imageUrl = storageService.uploadProfileImage(imageUri)

                // Update the current profile with the new image URL
                val currentProfile = _profileState.value.profile
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(
                        personalInfo = currentProfile.personalInfo.copy(
                            profileImageUrl = imageUrl
                        ),
                        updatedAt = Date()
                    )

                    // Save to Firestore
                    saveUserProfile(updatedProfile)
                }

                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    profile = currentProfile?.copy(
                        personalInfo = currentProfile.personalInfo.copy(profileImageUrl = imageUrl)
                    )
                )

            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to upload image: ${e.message}"
                )
            }
        }
    }
}
