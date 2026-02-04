package com.genius.shot.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DateHeader(date: String) {
    Surface(
        color = MaterialTheme.colorScheme.background, // 배경색을 앱 테마 배경색과 맞춤
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 24.dp, bottom = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 메인 날짜 텍스트 (예: 오늘, 2월 4일 (수))
            Text(
                text = date,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp // 자간을 살짝 좁혀서 단단한 느낌
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // 필요하다면 우측에 "선택" 버튼 등을 배치할 수 있는 공간
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}