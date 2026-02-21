package com.rivo.app.ui.screens.artist

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.model.Music
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.DashboardTab
import com.rivo.app.utils.ImagePickerHelper
import com.rivo.app.utils.MediaAccessHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDashboardScreen(
    onBackClick: () -> Unit,
    onEditTrackClick: (Music) -> Unit,
    onDeleteTrackClick: (Music) -> Unit,
    artistViewModel: ArtistViewModel
) {
    val artistMusic by artistViewModel.artistMusic.collectAsState()
    val artistAnalytics by artistViewModel.artistAnalytics.collectAsState()
    val isUploading by artistViewModel.isUploading.collectAsState()
    val uploadProgress by artistViewModel.uploadProgress.collectAsState()
    val operationStatus by artistViewModel.operationStatus.collectAsState()
    val selectedTab by artistViewModel.selectedTab.collectAsState()
    val context = LocalContext.current


    val snackbarHostState = remember { SnackbarHostState() }

    // Effect to show snackbar when operation status changes
    LaunchedEffect(operationStatus) {
        operationStatus?.let {
            snackbarHostState.showSnackbar(it)
            artistViewModel.clearOperationStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Dashboard", color = Color.White) },
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF2C2C2E),
                    contentColor = Color.White
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Tab selector - styled like admin panel
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Black,
                contentColor = Primary,
                indicator = { tabPositions ->
                    if (selectedTab.ordinal < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                                .height(3.dp)
                                .background(Primary)
                        )
                    }
                },
                divider = { Divider(thickness = 1.dp, color = Color(0xFF2C2C2E)) }
            ) {
                Tab(
                    selected = selectedTab == DashboardTab.UPLOAD_MUSIC,
                    onClick = { artistViewModel.setSelectedTab(DashboardTab.UPLOAD_MUSIC) },
                    text = {
                        Text(
                            "Upload Music",
                            color = Color.White,
                            fontWeight = if (selectedTab == DashboardTab.UPLOAD_MUSIC) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Tab(
                    selected = selectedTab == DashboardTab.MY_MUSIC,
                    onClick = { artistViewModel.setSelectedTab(DashboardTab.MY_MUSIC) },
                    text = {
                        Text(
                            "My Music",
                            color = Color.White,
                            fontWeight = if (selectedTab == DashboardTab.MY_MUSIC) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Tab(
                    selected = selectedTab == DashboardTab.ANALYTICS,
                    onClick = { artistViewModel.setSelectedTab(DashboardTab.ANALYTICS) },
                    text = {
                        Text(
                            "Analytics",
                            color = Color.White,
                            fontWeight = if (selectedTab == DashboardTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            when (selectedTab) {
                DashboardTab.UPLOAD_MUSIC -> UploadMusicTab(
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    onUploadMusic = { title, genre, description, audioUri, coverImageUri ->
                        Log.d("ArtistDashboard", "Upload button clicked with title: $title, genre: $genre")

                        if (audioUri != null) {
                            artistViewModel.uploadMusic(
                                context = context,
                                title = title,
                                genre = genre,
                                description = description,
                                audioUri = audioUri,
                                coverImageUri = coverImageUri
                            )
                        } else {
                            artistViewModel.clearOperationStatus()
                        }
                    }
                )
                DashboardTab.MY_MUSIC -> MyMusicTab(
                    artistMusic = artistMusic,
                    onEditTrackClick = onEditTrackClick,
                    onDeleteTrackClick = { music ->
                        artistViewModel.deleteMusic(music.id)
                    },
                    onUploadNewClick = { artistViewModel.setSelectedTab(DashboardTab.UPLOAD_MUSIC) }
                )
                DashboardTab.ANALYTICS -> AnalyticsTab(artistAnalytics = artistAnalytics)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMusicTab(
    isUploading: Boolean,
    uploadProgress: Float?,
    onUploadMusic: (String, String, String, Uri?, Uri?) -> Unit
) {
    val context = LocalContext.current
    var trackTitle by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageLocalPath by remember { mutableStateOf<String?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }

    val mediaAccessHelper = remember {
        if (context is FragmentActivity) {
            MediaAccessHelper(context)
        } else {
            null
        }
    }

    LaunchedEffect(isUploading) {
        if (!isUploading && uploadProgress == null) {
            if (audioUri != null) {
                trackTitle = ""
                genre = ""
                description = ""
                audioUri = null
                coverImageUri = null
                coverImageLocalPath = null
                audioFileName = null
            }
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Get file name for display
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex("_display_name")
                    if (displayNameIndex != -1) {
                        audioFileName = cursor.getString(displayNameIndex)
                    }
                }
            }

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val fileName = audioFileName ?: "audio_${System.currentTimeMillis()}.mp3"
                    val audioDir = java.io.File(context.filesDir, "audio")
                    if (!audioDir.exists()) {
                        audioDir.mkdirs()
                    }

                    val outputFile = java.io.File(audioDir, fileName)
                    inputStream.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    audioUri = Uri.fromFile(outputFile)
                    Log.d("AudioPicker", "Created local copy at: ${outputFile.absolutePath}")
                } else {
                    audioUri = uri
                    Log.d("AudioPicker", "Using original URI: $uri")
                }
            } catch (e: Exception) {
                Log.e("AudioPicker", "Error creating local copy: ${e.message}")
                audioUri = uri
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        coverImageUri = uri
        Log.d("ImagePicker", "Selected cover image URI: $uri")

        if (uri != null) {
            ImagePickerHelper.saveImageToInternalStorageAsync(
                context,
                uri,
                "cover_${System.currentTimeMillis()}.jpg"
            ) { localPath ->
                coverImageLocalPath = localPath
                Log.d("ImagePicker", "Saved image to: $localPath")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Upload Music",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Track Title",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = trackTitle,
            onValueChange = { trackTitle = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter track title", color = Color.Gray) },
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
            text = "Genre",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = genre,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Select genre", color = Color.Gray) },
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
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Genre",
                            tint = Color.White
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                val genres = listOf("Pop", "Rock", "Hip Hop", "R&B", "Jazz", "Classical", "Electronic", "Folk", "Country", "Reggae")
                genres.forEach { genreOption ->
                    DropdownMenuItem(
                        text = { Text(genreOption, color = Color.White) },
                        onClick = {
                            genre = genreOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Description",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("Tell us about your track", color = Color.Gray) },
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Audio File",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .clickable {
                    if (mediaAccessHelper != null) {
                        mediaAccessHelper.pickAudioFile { uri ->
                            Log.d("AudioPicker", "Selected audio URI via MediaAccessHelper: $uri")
                            audioUri = uri

                            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val displayNameIndex = cursor.getColumnIndex("_display_name")
                                    if (displayNameIndex != -1) {
                                        audioFileName = cursor.getString(displayNameIndex)
                                    }
                                }
                            }
                        }
                    } else {
                        audioPickerLauncher.launch("audio/*")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (audioUri != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio File",
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = audioFileName ?: "Audio file selected",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Tap to change",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload Audio",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Drag and drop your audio file\nhere or tap to browse",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "MP3, WAV, or FLAC up to 50MB",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cover Image Upload
        Text(
            text = "Cover Image",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
                .clickable {
                    // Launch the modern photo picker
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (coverImageUri != null) {
                // Use the local file path if available, otherwise fall back to URI
                // This helps with permission issues
                val imageModel = if (coverImageLocalPath != null) {
                    coverImageLocalPath
                } else {
                    coverImageUri
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload Cover Image",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Drag and drop your image file\nhere or tap to browse",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "JPG, PNG, or WEBP up to 5MB",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Upload progress
        if (isUploading && uploadProgress != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Button(
            onClick = {
                Log.d("UploadButton", "Button clicked with title: $trackTitle, genre: $genre")
                val finalCoverUri = if (coverImageLocalPath != null) {
                    Uri.parse("file://$coverImageLocalPath")
                } else {
                    coverImageUri
                }
                onUploadMusic(trackTitle, genre, description, audioUri, finalCoverUri)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isUploading && trackTitle.isNotBlank() && genre.isNotBlank() && audioUri != null
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isUploading) "Uploading..." else "Upload Track",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMusicTab(
    artistMusic: List<Music>,
    onEditTrackClick: (Music) -> Unit,
    onDeleteTrackClick: (Music) -> Unit,
    onUploadNewClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Music",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onUploadNewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Upload New", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search your music...", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Table header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Title",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(2f)
            )

            Text(
                text = "Album",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Genre",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Duration",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(0.5f)
            )

            Text(
                text = "Action",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.weight(0.5f)
            )
        }

        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

        // Music list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (artistMusic.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No music uploaded yet",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onUploadNewClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Your First Track", color = Color.White)
                        }
                    }
                }
            } else {
                val filteredMusic = if (searchQuery.isBlank()) {
                    artistMusic
                } else {
                    artistMusic.filter { music ->
                        music.title.contains(searchQuery, ignoreCase = true) ||
                                music.artist.contains(searchQuery, ignoreCase = true) ||
                                music.album?.contains(searchQuery, ignoreCase = true) == true ||
                                music.genre?.contains(searchQuery, ignoreCase = true) == true
                    }
                }

                filteredMusic.forEach { music ->
                    MusicListItem(
                        music = music,
                        onEditClick = { onEditTrackClick(music) },
                        onDeleteClick = { onDeleteTrackClick(music) }
                    )

                    Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun MusicListItem(
    music: Music,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art and title
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (music.artworkUri != null) {
                    AsyncImage(
                        model = music.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = music.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Album
        Text(
            text = music.album ?: "Single",
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Genre
        Text(
            text = music.genre ?: "Unknown",
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Duration
        Text(
            text = formatDuration(music.duration),
            color = Color.White,
            modifier = Modifier.weight(0.5f)
        )

        // Actions
        Box(
            modifier = Modifier.weight(0.5f)
        ) {
            IconButton(
                onClick = { showOptions = !showOptions },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Track Options",
                    tint = Primary
                )
            }

            DropdownMenu(
                expanded = showOptions,
                onDismissRequest = { showOptions = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Track", color = Color.White) },
                    onClick = {
                        onEditClick()
                        showOptions = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("View Analytics", color = Color.White) },
                    onClick = {
                        // Navigate to analytics
                        showOptions = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    onClick = {
                        onDeleteClick()
                        showOptions = false
                    }
                )
            }
        }
    }
}

@Composable
fun AnalyticsTab(artistAnalytics: ArtistAnalytics?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .background(Color.Black)
    ) {
        Text(
            text = "Analytics",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Check if analytics data is available
        if (artistAnalytics != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnalyticsCard(
                    title = "Total Plays",
                    value = formatNumber(artistAnalytics.totalPlays),
                    modifier = Modifier.weight(1f)
                )

                AnalyticsCard(
                    title = "Unique Listeners",
                    value = formatNumber(artistAnalytics.monthlyListeners),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnalyticsCard(
                    title = "Playlist Adds",
                    value = formatNumber(artistAnalytics.playlistAdds),
                    modifier = Modifier.weight(1f)
                )

                AnalyticsCard(
                    title = "Watchlist Saves",
                    value = formatNumber(artistAnalytics.watchlistSaves),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Analytics graph (optional)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Performance Analytics",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Top Songs section
            Text(
                text = "Top Performing Songs",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Top songs list (You can implement this part to show top songs)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No analytics data available yet",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            java.util.concurrent.TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}
