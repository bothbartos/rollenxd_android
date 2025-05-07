package com.bartosboth.rollen_android.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.ui.components.CoverImage
import com.bartosboth.rollen_android.ui.components.CustomButton
import com.bartosboth.rollen_android.ui.components.CustomTextField
import com.bartosboth.rollen_android.ui.components.ErrorMessage
import com.bartosboth.rollen_android.ui.components.ScreenContainer
import com.bartosboth.rollen_android.ui.components.WelcomeLogo
import com.bartosboth.rollen_android.ui.navigation.MainScreen

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
            }

            is LoginState.Error -> {
                showError = true
                errorMessage = (loginState as LoginState.Error).message
            }

            else -> {

            }
        }
    }

    ScreenContainer {

        WelcomeLogo()

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            imeAction = ImeAction.Next,
            onImeAction = { passwordFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(username, password)
                }
            },
            modifier = Modifier.focusRequester(passwordFocusRequester)
        )

        if (showError) {
            ErrorMessage(errorMessage)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = "Login",
            onClick = { viewModel.login(username, password) },
            isEnabled = username.isNotEmpty() && password.isNotEmpty(),
            isLoading = loginState is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign up")
        }
    }
}
