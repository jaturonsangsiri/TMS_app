package com.siamatic.tms.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.siamatic.tms.composables.login.LoginViewModel
import com.siamatic.tms.configs.RoutePath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(), controlRoute: NavHostController) {
  // State สำหรับ input fields
  var username by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var isLoading by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.loginResult) {
    uiState.loginResult?.let { result ->
      isLoading = false
      result.fold(
        onSuccess = {
          println("user: ${uiState.loginResult!!.getOrThrow().data.name}")
          Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
          // Navigate to HomePage
          controlRoute.navigate(route = RoutePath.Home.route) {
            popUpTo(RoutePath.Home.route) { inclusive = true }
          }
        },
        onFailure = { error ->
          Toast.makeText(context, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
        }
      )
    }
  }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Text("Login", fontSize = 32.sp, modifier = Modifier.padding(bottom = 32.dp))
    // user TextField
    OutlinedTextField(
      value = username,
      onValueChange = { username = it },
      label = { Text("Username") },
      placeholder = { Text("Enter your username") },
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
      singleLine = true,
      enabled = !isLoading
    )
    // Password TextField
    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      placeholder = { Text("Enter your password") },
      modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      trailingIcon = {
        val image = if (passwordVisible) Icons.Filled.Person else Icons.Filled.AccountCircle
        val description = if (passwordVisible) "Hide password" else "Show password"

        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(imageVector = image, contentDescription = description)
        }
      },
      singleLine = true,
      enabled = !isLoading
    )

    // Login Button
    Button(
      onClick = {
        if (username.isNotEmpty() && password.isNotEmpty()) {
          isLoading = true
          viewModel.performLogin(username, password)
        } else {
          Toast.makeText(context, "Please enter username and password", Toast.LENGTH_SHORT).show()
        }
      },
      modifier = Modifier.fillMaxWidth().height(50.dp),
      enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty()
    ) {
      if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
      } else {
        Text("Login", fontSize = 16.sp)
      }
    }

    uiState.errorMessage?.let { error ->
      Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 16.dp)
      )
    }
  }
}