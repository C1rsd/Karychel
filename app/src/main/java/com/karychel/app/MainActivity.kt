package com.karychel.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.karychel.app.ui.intro.IntroAnimation
import com.karychel.app.ui.library.LibraryScreen
import com.karychel.app.ui.reader.ReaderScreen
import com.karychel.app.ui.settings.SettingsScreen

/**
 * MainActivity: Host de navegación Compose con tema OLED negro.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBlackTheme()

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                AppNavHost()
            }
        }
    }

    private fun setupBlackTheme() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            isAppearanceLightNavigationBars = false
            isAppearanceLightStatusBars = false
        }
        window.statusBarColor = getColor(R.color.black_oled)
        window.navigationBarColor = getColor(R.color.black_oled)
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroAnimation {
                navController.navigate("library") {
                    popUpTo("intro") { inclusive = true }
                }
            }
        }
        composable("library") {
            LibraryScreen()
        }
        composable(
            route = "reader?path={path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType; nullable = false })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            ReaderScreen(
                chapterDirPath = path,
                onPanic = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}
