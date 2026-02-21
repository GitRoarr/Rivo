package com.rivo.app.ui.screens.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistVerificationScreen(
    userId: String,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val verificationStatus by viewModel.verificationStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val idDocumentUri by viewModel.idDocumentUri.collectAsState()
    val proofOfArtistryUri by viewModel.proofOfArtistryUri.collectAsState()
    val submissionSuccess by viewModel.submissionSuccess.collectAsState()
    
    var artistName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var location: String by remember { mutableStateOf("") }
    var primaryGenre by remember { mutableStateOf("") }
    var artistBio by remember { mutableStateOf("") }
    var instagramHandle by remember { mutableStateOf("") }
    var youtubeUsername by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var genreExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val idDocumentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setIdDocumentUri(it) }
    }
    
    val proofOfArtistryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setProofOfArtistryUri(it) }
    }
    
    LaunchedEffect(userId) {
        viewModel.checkVerificationStatus(userId)
    }
    
    LaunchedEffect(submissionSuccess) {
        if (submissionSuccess) {
            onSubmitSuccess()
            viewModel.clearSubmissionSuccess()
        }
    }
    
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            
            // Error message
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = error,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Verification status
            when (verificationStatus) {
                VerificationStatus.VERIFIED -> {
                    VerificationApprovedScreen()
                }
                VerificationStatus.PENDING -> {
                    VerificationPendingScreen()
                }
                VerificationStatus.REJECTED -> {
                    VerificationRejectedScreen(
                        onReapplyClick = { viewModel.checkVerificationStatus(userId) }
                    )
                }
                else -> {
                    // Verification form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Submit your information to verify your artist account and gain access to artist features.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Personal Information Section
                        Text(
                            text = "Personal Information",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Artist/Band Name
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
                        
                        // Email Address
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
                        
                        // Phone Number
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
                        
                        // Location
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
                        
                        // Music Information Section
                        Text(
                            text = "Music Information",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Primary Genre
                        Text(
                            text = "Primary Genre",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
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
                        
                        // Artist Bio
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
                        
                        // Social Media & Links Section
                        Text(
                            text = "Social Media & Links",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Instagram
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
                                .clickable { idDocumentPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (idDocumentUri != null) {
                                AsyncImage(
                                    model = idDocumentUri,
                                    contentDescription = "ID Document",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
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
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (idDocumentUri != null) "Document selected" else "No file chosen",
                            color = if (idDocumentUri != null) Primary else Color.Gray,
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
                                .clickable { proofOfArtistryPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (proofOfArtistryUri != null) {
                                AsyncImage(
                                    model = proofOfArtistryUri,
                                    contentDescription = "Proof of Artistry",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
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
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (proofOfArtistryUri != null) "Document selected" else "No file chosen",
                            color = if (proofOfArtistryUri != null) Primary else Color.Gray,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Terms and Conditions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = privacyAccepted,
                                onCheckedChange = { privacyAccepted = it },
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
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Submit Button
                        Button(
                            onClick = {
                                val socialLinks = mapOf(
                                    "instagram" to instagramHandle,
                                    "youtube" to youtubeUsername
                                )
                                
                                viewModel.submitVerification(
                                    userId = userId,
                                    artistName = artistName,
                                    email = email,
                                    phoneNumber = phoneNumber,
                                    location = location,
                                    primaryGenre = primaryGenre,
                                    artistBio = artistBio,
                                    socialLinks = socialLinks
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Primary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            enabled = termsAccepted && privacyAccepted && 
                                    artistName.isNotEmpty() && 
                                    idDocumentUri != null && 
                                    proofOfArtistryUri != null
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
                                imageVector = Icons.Default.Info,
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
        }
    }
}

@Composable
fun VerificationApprovedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verification Approved!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your artist account has been verified. You now have access to all artist features.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Upload Music",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You can now upload your music to the platform.",
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Analytics",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Access detailed analytics about your music and audience.",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationPendingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Pending,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verification Pending",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your verification request is being reviewed. This typically takes 1-3 business days.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.7f),
                color = Color.Yellow,
                trackColor = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Yellow
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Notification",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You'll receive an email when your verification status is updated.",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationRejectedScreen(
    onReapplyClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verification Rejected",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Unfortunately, your verification request was not approved. Please review the feedback and reapply.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Feedback",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "The provided documents were insufficient to verify your identity as an artist. Please provide clearer images and ensure all information is legible.",
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onReapplyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Reapply for Verification",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
