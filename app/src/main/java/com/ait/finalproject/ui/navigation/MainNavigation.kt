package com.ait.finalproject.ui.navigation

sealed class MainNavigation(val route: String) {
    object LoginScreen : MainNavigation("loginscreen")
    object MainScreen : MainNavigation("mainscreen")
}