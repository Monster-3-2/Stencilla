package com.stencilla.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stencilla.app.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StencillaOffWhite),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Logo / wordmark ──────────────────────────────────────────────
            Text(
                text = "stencilla",
                style = MaterialTheme.typography.headlineLarge,
                color = StencillaTerracotta,
            )
            Text(
                text = "your personal style curator",
                style = MaterialTheme.typography.bodyMedium,
                color = StencillaGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp),
            )

            // ── Tab toggle ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StencillaLightGray),
            ) {
                listOf("Sign In" to true, "Create Account" to false).forEach { (label, value) ->
                    val selected = isLogin == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) StencillaTerracotta else StencillaLightGray)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(onClick = { isLogin = value }) {
                            Text(
                                text = label,
                                color = if (selected) StencillaCardWhite else StencillaGray,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Form fields ─────────────────────────────────────────────────
            if (!isLogin) {
                StencillaTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                )
                Spacer(Modifier.height(16.dp))
            }

            StencillaTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(16.dp))

            StencillaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password visibility",
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            // ── Error ────────────────────────────────────────────────────────
            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Primary action button ────────────────────────────────────────
            Button(
                onClick = {
                    if (isLogin) viewModel.login(email.trim(), password)
                    else viewModel.register(email.trim(), password, fullName.trim().takeIf { it.isNotEmpty() })
                },
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StencillaTerracotta),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = StencillaCardWhite,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = if (isLogin) "Sign In" else "Create Account",
                        style = MaterialTheme.typography.labelLarge,
                        color = StencillaCardWhite,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = if (isLogin) "New here? Create a free account." else "Already have an account? Sign in.",
                style = MaterialTheme.typography.bodySmall,
                color = StencillaGray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StencillaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StencillaTerracotta,
            focusedLabelColor  = StencillaTerracotta,
            focusedLeadingIconColor  = StencillaTerracotta,
            unfocusedBorderColor     = StencillaLightGray,
            unfocusedLeadingIconColor = StencillaGray,
            cursorColor = StencillaTerracotta,
        ),
    )
}
