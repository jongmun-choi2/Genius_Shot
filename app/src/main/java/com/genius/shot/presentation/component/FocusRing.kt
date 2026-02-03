package com.genius.shot.presentation.component


import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun FocusRing(center: PointF) {
    // 등장할 때 약간 작아지는 애니메이션 (1.5배 -> 1.0배)
    val scaleAnim = remember { Animatable(1.5f) }

    LaunchedEffect(center) {
        scaleAnim.snapTo(1.5f)
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val squareSize = 80.dp.toPx() * scaleAnim.value
        val halfSize = squareSize / 2

        drawRoundRect(
            color = Color.Yellow,
            topLeft = Offset(center.x - halfSize, center.y - halfSize),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(10f, 10f), // 모서리 둥글게
            style = Stroke(width = 2.dp.toPx()) // 선 두께
        )
    }
}