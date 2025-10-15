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


@Composable
fun rememberSimpleImagePicker(
    onImageSelected: (Uri) -> Unit,
    onError: (String) -> Unit
): SimpleImagePickerState {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera 
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        android.util.Log.d("SimpleImagePicker", "Camera result: success=$success, uri=$currentImageUri")
        if (success && currentImageUri != null) {
            onImageSelected(currentImageUri!!)
        } else {
            onError("Failed to capture image")
        }
    }
    
    // Gallery 
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        android.util.Log.d("SimpleImagePicker", "Gallery result: uri=$uri")
        uri?.let { onImageSelected(it) }
    }
    
    // Permission 
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        android.util.Log.d("SimpleImagePicker", "Permission result: $permissions")
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        
        if (cameraGranted && storageGranted) {
            // Both permissions granted, can proceed with camera
            launchSimpleCamera(context, cameraLauncher) { uri ->
                currentImageUri = uri
            }
        } else {
            onError("Camera and storage permissions are required")
        }
    }
    
    return remember {
        SimpleImagePickerState(
            launchCamera = {
                android.util.Log.d("SimpleImagePicker", "Launch camera clicked")
                val hasCamera = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                val hasStorage = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                
                android.util.Log.d("SimpleImagePicker", "Permissions - Camera: $hasCamera, Storage: $hasStorage")
                
                if (hasCamera && hasStorage) {
                    android.util.Log.d("SimpleImagePicker", "Permissions granted, launching camera")
                    launchSimpleCamera(context, cameraLauncher) { uri ->
                        currentImageUri = uri
                    }
                } else {
                    android.util.Log.d("SimpleImagePicker", "Requesting permissions")
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    )
                }
            },
            launchGallery = {
                android.util.Log.d("SimpleImagePicker", "Launch gallery clicked")
                val hasStorage = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                
                android.util.Log.d("SimpleImagePicker", "Storage permission: $hasStorage")
                
                if (hasStorage) {
                    android.util.Log.d("SimpleImagePicker", "Storage permission granted, launching gallery")
                    galleryLauncher.launch("image/*")
                } else {
                    android.util.Log.d("SimpleImagePicker", "Requesting storage permission")
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
        )
    }
}


data class SimpleImagePickerState(
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)


private fun launchSimpleCamera(
    context: Context,
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onUriCreated: (Uri) -> Unit
) {
    try {
        android.util.Log.d("SimpleImagePicker", "Creating image file")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        
        android.util.Log.d("SimpleImagePicker", "Image URI created: $imageUri")
        onUriCreated(imageUri)
        android.util.Log.d("SimpleImagePicker", "Launching camera with URI: $imageUri")
        cameraLauncher.launch(imageUri)
    } catch (e: Exception) {
        android.util.Log.e("SimpleImagePicker", "Error launching camera", e)
    }
}
