package com.example.fit5046_app

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageService {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Upload a profile image to Firebase Storage
     * @param imageUri The URI of the image to upload
     * @return The download URL of the uploaded image
     */
    suspend fun uploadProfileImage(imageUri: Uri): String {
        val user = auth.currentUser ?: throw Exception("User not authenticated")
        val userId = user.uid

        // Create a unique filename
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("profile_images/$userId/$fileName")

        // Upload the file
        val uploadTask = storageRef.putFile(imageUri).await()

        // Get the download URL
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Delete a profile image from Firebase Storage
     * @param imageUrl The URL of the image to delete
     */
    suspend fun deleteProfileImage(imageUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            // Log error but don't throw - image might already be deleted
            println("Error deleting image: ${e.message}")
        }
    }

    /**
     * Get a reference to the user's profile images folder
     */
    fun getProfileImagesRef(): StorageReference {
        val user = auth.currentUser ?: throw Exception("User not authenticated")
        return storage.reference.child("profile_images")
    }

    /**
     * Check if a URL is a Firebase Storage URL
     */
    fun isFirebaseStorageUrl(url: String): Boolean {
        return url.contains("firebasestorage.googleapis.com")
    }
}
