package com.ait.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ait.finalproject.ui.screen.login.LoginScreen
import com.ait.finalproject.ui.screen.main.MainScreen
import com.ait.finalproject.ui.screen.navigation.MainNavigation
import com.ait.finalproject.ui.theme.FinalProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Greeting("Android")
                    MainAppNavHost()
                }
            }
        }
    }
}

@Composable
fun MainAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController,
        startDestination = MainNavigation.LoginScreen.route) {

        composable(MainNavigation.LoginScreen.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(MainNavigation.MainScreen.route)
            })
        }
        composable(MainNavigation.MainScreen.route) {
            MainScreen()
        }
    }

}