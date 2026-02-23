package com.rivo.app.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.screens.home.PremiumArtistCard
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel

@Composable
fun ArtistListScreen(
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    artistViewModel: ArtistViewModel = hiltViewModel(),
    followViewModel: FollowViewModel = hiltViewModel()
) {
    val artists by artistViewModel.artists.collectAsState()

    Scaffold(
        topBar = {
            RivoTopBar(
                title = "Featured Artists",
                onBackClick = onBackClick
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C0C12), Color.Black)
                    )
                )
        ) {
            if (artists.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No artists yet",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    itemsIndexed(artists.chunked(2)) { _, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            val first = row.getOrNull(0)
                            val second = row.getOrNull(1)

                            if (first != null) {
                                val isFollowing by followViewModel.isFollowingArtist(first.id).collectAsState()
                                val followerCount by followViewModel.getArtistFollowerCount(first.id).collectAsState()
                                PremiumArtistCard(
                                    artist = first,
                                    isFollowing = isFollowing,
                                    followerCount = followerCount,
                                    onFollowClick = { followViewModel.toggleFollow(first.id) },
                                    onClick = { onArtistClick(first.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (second != null) {
                                val isFollowing by followViewModel.isFollowingArtist(second.id).collectAsState()
                                val followerCount by followViewModel.getArtistFollowerCount(second.id).collectAsState()
                                PremiumArtistCard(
                                    artist = second,
                                    isFollowing = isFollowing,
                                    followerCount = followerCount,
                                    onFollowClick = { followViewModel.toggleFollow(second.id) },
                                    onClick = { onArtistClick(second.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
