package com.genius.shot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.genius.shot.presentation.camera.component.PermissionWrapper
import com.genius.shot.presentation.camera.screen.CameraScreen
import com.genius.shot.presentation.gallery.screen.GalleryScreen
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
                    GeniusShotAppNavHost()
                }
            }
        }
    }
}

@Composable
fun GeniusShotAppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "camera_route") {
        composable("camera_route") {
            CameraScreen(
                onGalleryClick = {
                    navController.navigate("gallery_route")
                }
            )
        }

        composable("gallery_route") {
            GalleryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

    }
}