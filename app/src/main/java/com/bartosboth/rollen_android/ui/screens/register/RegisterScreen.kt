package com.bartosboth.rollen_android.ui.screens.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bartosboth.rollen_android.ui.components.CustomButton
import com.bartosboth.rollen_android.ui.components.CustomTextField
import com.bartosboth.rollen_android.ui.components.ErrorMessage
import com.bartosboth.rollen_android.ui.components.ScreenContainer
import com.bartosboth.rollen_android.ui.components.WelcomeLogo

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> onRegisterSuccess()
            is RegisterState.Error -> {
                errorMessage = (registerState as RegisterState.Error).message
                showError = true
            }
            else -> {  }
        }
    }

    ScreenContainer {

        WelcomeLogo()

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        CustomTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            imeAction = ImeAction.Next,
            onImeAction = { emailFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { passwordFocusRequester.requestFocus() },
            modifier = Modifier.focusRequester(emailFocusRequester)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            imeAction = ImeAction.Next,
            onImeAction = { confirmPasswordFocusRequester.requestFocus() },
            modifier = Modifier.focusRequester(passwordFocusRequester)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = {
                if (username.isNotEmpty() && email.isNotEmpty() &&
                    password.isNotEmpty() && password == confirmPassword) {
                    viewModel.register(username, email, password)
                }
            },
            modifier = Modifier.focusRequester(confirmPasswordFocusRequester)
        )

        if (showError) {
            ErrorMessage(errorMessage)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = "Register",
            onClick = { viewModel.register(username, email, password) },
            isEnabled = username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty() && password == confirmPassword,
            isLoading = registerState is RegisterState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in")
        }
    }
}
