package com.example.fit5046_app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ImagePicker(private val context: Context) {
    
    
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    
    
    fun getImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
    
    
    fun hasCameraPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("ImagePicker", "Camera permission: $hasPermission")
        return hasPermission
    }
    
    
    fun hasStoragePermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
        android.util.Log.d("ImagePicker", "Storage permission: $hasPermission")
        return hasPermission
    }
}


@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit,
    onError: (String) -> Unit
): ImagePickerState {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = remember { ImagePicker(context) }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        android.util.Log.d("ImagePicker", "Camera result: success=$success, uri=$currentImageUri")
        if (success && currentImageUri != null) {
            onImageSelected(currentImageUri!!)
        } else {
            onError("Failed to capture image")
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        android.util.Log.d("ImagePicker", "Gallery result: uri=$uri")
        uri?.let { onImageSelected(it) }
    }
    
    // Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        
        if (cameraGranted && storageGranted) {
            // Both permissions granted, can proceed with camera
            launchCamera(imagePicker, cameraLauncher) { uri ->
                currentImageUri = uri
            }
        } else {
            onError("Camera and storage permissions are required")
        }
    }
    
    return remember {
        ImagePickerState(
            imagePicker = imagePicker,
            launchCamera = {
                android.util.Log.d("ImagePicker", "Launch camera clicked")
                if (imagePicker.hasCameraPermission() && imagePicker.hasStoragePermission()) {
                    android.util.Log.d("ImagePicker", "Permissions granted, launching camera")
                    launchCamera(imagePicker, cameraLauncher) { uri ->
                        currentImageUri = uri
                    }
                } else {
                    android.util.Log.d("ImagePicker", "Requesting permissions")
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    )
                }
            },
            launchGallery = {
                android.util.Log.d("ImagePicker", "Launch gallery clicked")
                if (imagePicker.hasStoragePermission()) {
                    android.util.Log.d("ImagePicker", "Storage permission granted, launching gallery")
                    galleryLauncher.launch("image/*")
                } else {
                    android.util.Log.d("ImagePicker", "Requesting storage permission")
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
        )
    }
}


data class ImagePickerState(
    val imagePicker: ImagePicker,
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)


private fun launchCamera(
    imagePicker: ImagePicker, 
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onUriCreated: (Uri) -> Unit
) {
    try {
        android.util.Log.d("ImagePicker", "Creating image file")
        val imageFile = imagePicker.createImageFile()
        val imageUri = imagePicker.getImageUri(imageFile)
        android.util.Log.d("ImagePicker", "Image URI created: $imageUri")
        onUriCreated(imageUri)
        android.util.Log.d("ImagePicker", "Launching camera with URI: $imageUri")
        cameraLauncher.launch(imageUri)
    } catch (e: Exception) {
        android.util.Log.e("ImagePicker", "Error launching camera", e)
    }
}
