package com.genius.shot.presentation.component

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.genius.shot.domain.model.PermissionState
import com.genius.shot.presentation.dialog.RationaleDialog
import com.genius.shot.presentation.dialog.SettingsDialog

@Composable
fun PermissionWrapper (
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf<PermissionState>(PermissionState.Denied) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionStatus = PermissionState.Granted
        } else {
            val activity = context as Activity
            val showRationale = activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            permissionStatus = if (showRationale) {
                PermissionState.ShouldShowRationale
            } else {
                PermissionState.PermanentlyDenied
            }
        }
    }

    // 초기 체크 및 실행
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                permissionStatus = PermissionState.Granted
            }
            else -> launcher.launch(Manifest.permission.CAMERA)
        }
    }

    when (permissionStatus) {
        is PermissionState.Granted -> onPermissionGranted()
        is PermissionState.ShouldShowRationale -> {
            // 여기에 커스텀 다이얼로그를 띄워 재요청 유도
            RationaleDialog(onConfirm = { launcher.launch(Manifest.permission.CAMERA) })
        }
        is PermissionState.PermanentlyDenied -> {
            // 설정창으로 유도하는 다이얼로그
            SettingsDialog(onConfirm = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            })
        }
        else -> { /* 로딩 중 혹은 대기 상태 */ }
    }
}