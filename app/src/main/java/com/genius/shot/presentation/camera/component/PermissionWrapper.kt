package com.genius.shot.presentation.camera.component

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.genius.shot.domain.model.PermissionState
import com.genius.shot.presentation.camera.dialog.RationaleDialog
import com.genius.shot.presentation.camera.dialog.SettingsDialog

@Composable
fun PermissionWrapper (
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current



    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }

    var arePermissionsGranted by remember { mutableStateOf(false) }
    var showSettingDialog by remember { mutableStateOf(false) }

    fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        arePermissionsGranted = allGranted

        if(!allGranted) {
            val activity = context as? Activity
            val shouldShowRationale = permissions.any { permission ->
                activity?.shouldShowRequestPermissionRationale(permission) == true
            }

            if(!shouldShowRationale) {
                showSettingDialog = true
            }

        }

    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkPermissions()) {
                    arePermissionsGranted = true
                    showSettingDialog = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 초기 체크 및 실행
    LaunchedEffect(Unit) {
        if (checkPermissions()) {
            arePermissionsGranted = true
        } else {
            launcher.launch(permissions)
        }
    }
    if (arePermissionsGranted) {
        onPermissionGranted()
    }else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (showSettingDialog) {
                SettingsDialog(
                    onConfirm = {
                        // 앱 설정 화면으로 이동하는 인텐트
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onDismiss = { showSettingDialog = false }
                )
            } else {
                RationaleDialog(
                    onConfirm = {
                        launcher.launch(permissions)
                    }
                )
            }
        }
    }
}