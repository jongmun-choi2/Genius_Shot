package com.genius.shot.presentation.camera.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun CameraSystemDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            onDismiss?.let {
                TextButton(onClick = it) {
                    Text("취소")
                }
            }
        }

    )
}

@Composable
fun RationaleDialog(onConfirm: () -> Unit) {
    CameraSystemDialog(
        title = "카메라 권한 필요",
        text = "멋진 사진을 찍기 위해서는 카메라 권한이 꼭 필요합니다. 다시 요청할까요?",
        confirmText = "다시 시도",
        onConfirm = onConfirm
    )
}

@Composable
fun SettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CameraSystemDialog(
        title = "권한 설정 필요",
        text = "카메라 권한이 거부되어 기능을 사용할 수 없습니다. 설정 화면에서 권한을 허용해 주세요.",
        confirmText = "설정으로 이동",
        onConfirm = onConfirm
    )
}
