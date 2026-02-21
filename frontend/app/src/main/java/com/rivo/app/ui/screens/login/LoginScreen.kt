package com.rivo.app.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoLogo
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.ui.viewmodel.LoginState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            authViewModel.resetLoginState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated Background Elements
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .size(300.dp)
                .background(RivoPurple.copy(alpha = 0.2f), CircleShape)
                .blur(80.dp)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 50.dp)
                .size(350.dp)
                .background(RivoPink.copy(alpha = 0.15f), CircleShape)
                .blur(80.dp)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logo and Welcome
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RivoLogo(
                        size = 80.dp,
                        showText = false,
                        animated = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = "Music tailored to your soul",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Form
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    placeholder = "hello@rivo.com"
                )

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    placeholder = "••••••••",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = it }
                )

                // Forgot Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = RivoPink,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onForgotPasswordClick() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && loginState !is LoginState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RivoPink,
                        disabledContainerColor = RivoPink.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (loginState is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Log In",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Error Message
                AnimatedVisibility(visible = loginState is LoginState.Error) {
                    val error = (loginState as? LoginState.Error)?.message ?: ""
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = error, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RivoPink,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedBorderColor = RivoPink,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = RivoPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (value.isNotEmpty()) RivoPink else Color.Gray
                )
            },
            trailingIcon = if (isPassword && onPasswordVisibilityChange != null) {
                {
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            placeholder = {
                Text(text = placeholder, color = Color.Gray.copy(alpha = 0.5f))
            },
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
            )
        )
    }
}
