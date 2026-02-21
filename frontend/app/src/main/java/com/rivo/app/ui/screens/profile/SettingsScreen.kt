package com.rivo.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("Dark") }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            RivoTopBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
            // App Language
            Text(
                text = "App Language",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            var langMenuExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = langMenuExpanded,
                onExpandedChange = { langMenuExpanded = !langMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
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

                ExposedDropdownMenu(
                    expanded = langMenuExpanded,
                    onDismissRequest = { langMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    DropdownMenuItem(
                        text = { Text("English", color = Color.White) },
                        onClick = {
                            selectedLanguage = "English"
                            langMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Amharic", color = Color.White) },
                        onClick = {
                            selectedLanguage = "Amharic"
                            langMenuExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications
            Text(
                text = "Notifications",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enable Notifications",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF444444)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme
            Text(
                text = "Theme",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            var themeExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = themeExpanded,
                onExpandedChange = { themeExpanded = !themeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTheme,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
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

                ExposedDropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    DropdownMenuItem(
                        text = { Text("Dark", color = Color.White) },
                        onClick = {
                            selectedTheme = "Dark"
                            themeExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Light", color = Color.White) },
                        onClick = {
                            selectedTheme = "Light"
                            themeExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Update Settings Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Settings updated: $selectedLanguage, ${if (notificationsEnabled) "Notifications ON" else "Notifications OFF"}, $selectedTheme Theme"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Update Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
