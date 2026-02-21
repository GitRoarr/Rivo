package com.rivo.app
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.rivo.app.ui.RivoApp
import com.rivo.app.ui.theme.RivoTheme
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.app.AlertDialog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requiredPermissions = mutableListOf<String>()
    private var permissionsGranted = mutableStateOf(false)

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val denied = permissions.filter { !it.value }.keys
        permissionsGranted.value = denied.isEmpty()

        if (permissionsGranted.value) {
            Log.d("MainActivity", "All permissions granted")
        } else {
            Log.d("MainActivity", "Permissions denied: $denied")
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissions()
        checkAndRequestPermissions()

        setContent {
            RivoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RivoApp()
                }
            }
        }
    }

    private fun setupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            permissionsGranted.value = true
            Log.d("MainActivity", "All permissions already granted")
        } else {
            val showRationale = permissionsToRequest.any {
                shouldShowRequestPermissionRationale(it)
            }

            if (showRationale) {
                showPermissionRationaleDialog(permissionsToRequest)
            } else {
                requestPermissionsLauncher.launch(permissionsToRequest)
            }
        }
    }

    private fun showPermissionRationaleDialog(permissionsToRequest: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permissions Needed")
            .setMessage("This app requires access to your media and notifications to function properly.")
            .setPositiveButton("Allow") { dialog, which ->
                requestPermissionsLauncher.launch(permissionsToRequest)
            }
            .setNegativeButton("Cancel") { dialog, which -> }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Denied")
            .setMessage("Some permissions are permanently denied. Please enable them in App Settings.")
            .setPositiveButton("Open Settings") { dialog, which ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, which -> }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
