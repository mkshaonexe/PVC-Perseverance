package com.perseverance.pvc.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoBackground(
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    // Only prepare video player in dark theme
    DisposableEffect(context, isLightTheme) {
        if (!isLightTheme) {
            val player = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri("file:///android_asset/focus_1_400.mp4")
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false // Start paused for preview
                prepare()
            }
            exoPlayer = player
        }
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }
    
    // Update play state when isPlaying changes
    LaunchedEffect(isPlaying, isLightTheme) {
        if (!isLightTheme) {
            exoPlayer?.let { player ->
                if (isPlaying) player.play() else player.pause()
            }
        }
    }
    
    if (isLightTheme) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    } else {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
