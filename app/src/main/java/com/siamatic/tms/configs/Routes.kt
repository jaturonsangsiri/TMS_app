package com.siamatic.tms.configs

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.siamatic.tms.pages.LoginScreen
import com.siamatic.tms.composables.login.LoginViewModel
import com.siamatic.tms.pages.HomePage

// Settings route path menage
@Composable
fun Routes(controlRoute: NavHostController) {
  val viewModel: LoginViewModel = viewModel()
  NavHost(
      controlRoute,
      startDestination = RoutePath.Home.route
  ) {
    composable(RoutePath.Login.route) { LoginScreen(viewModel, controlRoute) }
    composable(RoutePath.Home.route) { HomePage(controlRoute) }
  }
}