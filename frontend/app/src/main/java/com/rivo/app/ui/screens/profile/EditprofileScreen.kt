package com.rivo.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userViewModel: UserViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val isLoading by userViewModel.isLoading.collectAsState()  // To handle the spinner state
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load user data
    LaunchedEffect(Unit) {
        userViewModel.currentUser.collect { user ->
            user?.let {
                displayName = it.fullName
                bio = it.bio ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            RivoTopBar(
                title = "Edit Profile",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Display Name",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                placeholder = { Text("Enter your stage name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF222222),
                    unfocusedContainerColor = Color(0xFF222222)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bio",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = { Text("Tell your fans something cool...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF222222),
                    unfocusedContainerColor = Color(0xFF222222)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        userViewModel.updateUserProfile(
                            fullName = displayName,
                            bio = bio
                        ) { success, errorMessage ->
                            if (success) {
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            } else {
                                Toast.makeText(context, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Save Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

