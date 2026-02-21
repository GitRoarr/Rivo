package com.rivo.app.ui.screens.register

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.rivo.app.data.model.UserType
import com.rivo.app.ui.components.RivoLogo
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.ui.viewmodel.RegisterState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: AuthViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var userType by remember { mutableStateOf(UserType.LISTENER) }

    var isPasswordValid by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

    val registerState by viewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            isPasswordValid = viewModel.validatePassword(password)
            if (!isPasswordValid) {
                passwordError = viewModel.getPasswordErrorMessage(password)
            }
        }
    }

    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            isEmailValid = viewModel.validateEmail(email)
        }
    }

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            onRegisterSuccess()
            viewModel.resetRegisterState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Decorative Elements
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-20).dp)
                .size(350.dp)
                .background(RivoPurple.copy(alpha = 0.15f), CircleShape)
                .blur(80.dp)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 60.dp)
                .size(300.dp)
                .background(RivoPink.copy(alpha = 0.1f), CircleShape)
                .blur(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Intro
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RivoLogo(
                        size = 64.dp,
                        showText = false,
                        animated = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = "Join the Rivo community",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    placeholder = "John Doe"
                )

                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    placeholder = "hello@rivo.com",
                    isError = email.isNotEmpty() && !isEmailValid,
                    errorText = if (email.isNotEmpty() && !isEmailValid) "Please enter a valid email" else null
                )

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    placeholder = "••••••••",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = it },
                    isError = password.isNotEmpty() && !isPasswordValid
                )

                // Password Requirements
                AnimatedVisibility(visible = password.isNotEmpty() && !isPasswordValid) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Password must contain:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            PasswordRequirement("8+ characters", password.length >= 8)
                            PasswordRequirement("Uppercase letter", password.any { it.isUpperCase() })
                            PasswordRequirement("Lowercase letter", password.any { it.isLowerCase() })
                            PasswordRequirement("Number", password.any { it.isDigit() })
                            PasswordRequirement("Special character", password.any { !it.isLetterOrDigit() })
                        }
                    }
                }

                PremiumTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    icon = Icons.Default.Lock,
                    placeholder = "••••••••",
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = { confirmPasswordVisible = it },
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    errorText = if (confirmPassword.isNotEmpty() && password != confirmPassword) "Passwords do not match" else null
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account Type Selector
                AccountTypeSelector(
                    selectedType = userType,
                    onTypeSelected = { userType = it }
                )
                
                if (userType == UserType.ARTIST) {
                    Text(
                        text = "Artist accounts require verification and approval.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = {
                        if (password == confirmPassword && isPasswordValid && isEmailValid) {
                            viewModel.register(fullName, email, password, userType)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = fullName.isNotEmpty() && email.isNotEmpty() &&
                            password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                            password == confirmPassword && isPasswordValid && isEmailValid &&
                            registerState !is RegisterState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RivoPink,
                        disabledContainerColor = RivoPink.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (registerState is RegisterState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Error Message Display
                AnimatedVisibility(visible = registerState is RegisterState.Error) {
                    val error = (registerState as? RegisterState.Error)?.message ?: ""
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
                
                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RivoPink,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PasswordRequirement(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isMet) Color.White else Color.Gray
            )
        )
    }
}

@Composable
fun AccountTypeSelector(
    selectedType: UserType,
    onTypeSelected: (UserType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserType.values().filter { it != UserType.ADMIN && it != UserType.GUEST }.forEach { type ->
            val isSelected = selectedType == type
            val label = if (type == UserType.LISTENER) "Listener" else "Artist"
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) RivoPink.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onTypeSelected(type) }
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) RivoPink else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) RivoPink else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
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
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null
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
                focusedBorderColor = if (isError) Color(0xFFEF4444) else RivoPink,
                unfocusedBorderColor = if (isError) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                cursorColor = RivoPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorLabelColor = Color(0xFFEF4444),
                errorLeadingIconColor = Color(0xFFEF4444)
            ),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) Color(0xFFEF4444) else if (value.isNotEmpty()) RivoPink else Color.Gray
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
            ),
            isError = isError,
            supportingText = if (errorText != null) {
                { Text(text = errorText, color = Color(0xFFEF4444)) }
            } else null
        )
    }
}