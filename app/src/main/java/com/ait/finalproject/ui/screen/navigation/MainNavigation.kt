package com.ait.finalproject.ui.screen.navigation

sealed class MainNavigation(val route: String) {
    object LoginScreen : MainNavigation("loginscreen")
    object MainScreen : MainNavigation("mainscreen")
}