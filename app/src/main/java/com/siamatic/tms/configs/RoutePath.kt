package com.siamatic.tms.configs

// Settings path of this application
sealed class RoutePath(val route: String) {
    // Home Path
    data object Home : RoutePath(route = "Home")
}