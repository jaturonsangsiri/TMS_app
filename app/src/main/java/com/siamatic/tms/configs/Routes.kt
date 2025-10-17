package com.siamatic.tms.configs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.siamatic.tms.pages.HomePage

// Settings route path menage
@Composable
fun Routes(controlRoute: NavHostController) {
  NavHost(
      controlRoute,
      startDestination = RoutePath.Home.route
  ) {
    composable(RoutePath.Home.route) { HomePage(controlRoute) }
  }
}