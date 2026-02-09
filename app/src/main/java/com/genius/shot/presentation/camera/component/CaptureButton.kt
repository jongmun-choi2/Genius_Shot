package com.genius.shot.presentation.camera.component

import android.media.MediaActionSound
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CaptureButton(
    onClick: () -> Unit,
    isEnabled: Boolean = true, // 촬영 중일 때 비활성화용
    modifier: Modifier = Modifier
) {

    val shutterSound = remember { MediaActionSound() }

    // ✨ 2. 메모리 관리 (화면 나갈 때 해제)
    DisposableEffect(Unit) {
        // 미리 로드해두면 딜레이 없이 즉시 소리가 납니다.
        shutterSound.load(MediaActionSound.SHUTTER_CLICK)

        onDispose {
            shutterSound.release() // 꼭 해제해야 메모리 누수 방지
        }
    }

    Box(
        modifier = modifier
            .size(80.dp) // 전체 크기
            .border(4.dp, Color.White, CircleShape) // 바깥 테두리
            .padding(6.dp) // 테두리와 내부 원 사이 간격
            .border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape) // 미세한 그림자 효과
    ) {
        // 내부 원 (실제 버튼)
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, Color.White, CircleShape)
                .clickable(
                    enabled = isEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 32.dp, color = Color.Gray)
                ) {
                    shutterSound.play(MediaActionSound.SHUTTER_CLICK)
                    onClick()
                }
                .align(Alignment.Center)
        )
    }
}