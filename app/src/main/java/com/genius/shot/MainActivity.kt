package com.genius.shot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.genius.shot.presentation.camera.component.PermissionWrapper
import com.genius.shot.presentation.camera.screen.CameraScreen
import com.genius.shot.presentation.gallery.screen.DetailScreen
import com.genius.shot.presentation.gallery.screen.DuplicateCheckScreen
import com.genius.shot.presentation.gallery.screen.GalleryScreen
import com.genius.shot.ui.theme.GeniusShotTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

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
                },
                onImageClick = { uri ->
                    val imagePath = URLEncoder.encode(uri.toString(), "UTF-8")
                    navController.navigate("detail_route/$imagePath")
                },
                onCleanClick = {
                    navController.navigate("duplicate_check_route")
                }
            )
        }

        composable(
            route = "detail_route/{imagePath}",
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
            DetailScreen(
                imagePath = imagePath,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = "duplicate_check_route") {
            DuplicateCheckScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}