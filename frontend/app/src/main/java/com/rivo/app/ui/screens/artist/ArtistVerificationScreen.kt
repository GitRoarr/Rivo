package com.rivo.app.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistVerificationScreen(
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    var artistName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var primaryGenre by remember { mutableStateOf("") }
    var artistBio by remember { mutableStateOf("") }
    var instagramHandle by remember { mutableStateOf("") }
    var youtubeUsername by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Verification", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Submit your information to verify your artist account and gain access to artist features.",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Personal Information",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Artist/Band Name*",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your artist name", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Email Address",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your email address", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Phone Number",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your phone number", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Location",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("City", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Music Information",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Primary Genre",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var genreExpanded by remember { mutableStateOf(false) }
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = primaryGenre,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Select your primary genre", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Primary,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { genreExpanded = !genreExpanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Genre",
                                tint = Color.White
                            )
                        }
                    }
                )
                
                DropdownMenu(
                    expanded = genreExpanded,
                    onDismissRequest = { genreExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    val genres = listOf("Pop", "Rock", "Hip Hop", "R&B", "Jazz", "Classical", "Electronic", "Folk", "Country", "Reggae")
                    genres.forEach { genreOption ->
                        DropdownMenuItem(
                            text = { Text(genreOption, color = Color.White) },
                            onClick = {
                                primaryGenre = genreOption
                                genreExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Artist Bio",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = artistBio,
                onValueChange = { artistBio = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Write about yourself and your music (200 words)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Social Media & Links",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Instagram",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = instagramHandle,
                onValueChange = { instagramHandle = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Instagram username", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // YouTube
            Text(
                text = "YouTube",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = youtubeUsername,
                onValueChange = { youtubeUsername = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your YouTube username", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Verification Documents Section
            Text(
                text = "Verification Documents",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Submit a photo of your ID to verify your identity as an artist/band member.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ID Document Upload
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable { /* Handle ID upload */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload ID",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Upload ID",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No file chosen",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Proof of Artistry Section
            Text(
                text = "Proof of Artistry",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Album cover, created poster, music distribution contract, etc.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Proof Upload
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable { /* Handle proof upload */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload Proof",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Upload proof",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No file chosen",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms and Conditions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                val termsText = buildAnnotatedString {
                    append("I agree to the ")
                    pushStyle(SpanStyle(color = Primary, textDecoration = TextDecoration.Underline))
                    append("terms and conditions")
                    pop()
                    append(" and ")
                    pushStyle(SpanStyle(color = Primary, textDecoration = TextDecoration.Underline))
                    append("privacy policy")
                    pop()
                    append(".")
                }
                
                Text(
                    text = termsText,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "I confirm that all information provided is correct and authentic.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit Button
            Button(
                onClick = onSubmitClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = termsAccepted && artistName.isNotEmpty()
            ) {
                Text(
                    text = "Submit for Verification",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Verification status note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Verification typically takes 1-3 business days.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = "You'll receive an email when your verification status is updated.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
