package com.genius.shot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.genius.shot.presentation.component.PermissionWrapper
import com.genius.shot.presentation.screen.CameraScreen
import com.genius.shot.ui.theme.GeniusShotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전체 화면 모드 (Edge-to-Edge) 설정
        enableEdgeToEdge()

        setContent {
            GeniusShotTheme {
                PermissionWrapper {
                    // 메인 화면으로 CameraScreen 호출
                    CameraScreen(
                        onGalleryClick = {
                            // TODO: 갤러리 화면으로 이동하는 Navigation 로직
                            Log.d("MainActivity", "Gallery Clicked")
                        }
                    )
                }
            }
        }
    }
}