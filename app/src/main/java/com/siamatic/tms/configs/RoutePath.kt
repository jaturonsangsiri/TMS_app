package com.siamatic.tms.configs

// Settings path of this application
sealed class RoutePath(val route: String) {
    // Login Path
    data object Login : RoutePath(route = "Login")
    // Home Path
    data object Home : RoutePath(route = "Home")
}