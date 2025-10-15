package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perseverance.pvc.R
import com.perseverance.pvc.ui.components.VideoBackground
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.viewmodel.PomodoroViewModel

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Video background (always visible - preview when paused, playing when active)
        VideoBackground(
            isPlaying = uiState.isPlaying,
            modifier = Modifier.fillMaxSize()
        )
        
        // Semi-transparent overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "pomodoro",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Timer display
            Text(
                text = uiState.timeDisplay,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < uiState.completedSessions) 
                                    Color(0xFF4CAF50) 
                                else 
                                    Color(0xFFE0E0E0)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Cat illustration placeholder (simplified version)
            CatIllustration()
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Play/Pause button positioned in the red marked area
            Button(
                onClick = {
                    if (uiState.isPlaying) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 40.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (uiState.isPlaying) "⏸ Pause" else "▶ Start Focus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "⏰",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = "📅",
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = "👤",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CatIllustration() {
    // Sleep illustration
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sleep_illustration),
            contentDescription = "Sleep illustration",
            modifier = Modifier.size(180.dp),
            colorFilter = ColorFilter.tint(Color(0xFFFF8C42))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PomodoroScreenPreview() {
    PerseverancePVCTheme {
        PomodoroScreen()
    }
}
