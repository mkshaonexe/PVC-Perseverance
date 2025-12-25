package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.components.GlobalMissionCard
import com.perseverance.pvc.ui.components.CustomMissionItem
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.LaunchedEffect
import com.perseverance.pvc.utils.AnalyticsHelper

@Composable
fun GroupScreen(
    socialViewModel: com.perseverance.pvc.ui.viewmodel.SocialViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    val uiState by socialViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        // AnalyticsHelper.logScreenView("GroupScreen")
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Simple background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        
        // Semi-transparent overlay
        Box(
             modifier = Modifier
                .fillMaxSize()
                .background(
                    if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.Black.copy(alpha = 0.5f)
                    else
                        Color.Transparent
                )
        )
        
        if (!uiState.isSignedIn) {
            // Login Screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Join the Club",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign in to join missions and study with friends.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { socialViewModel.performGoogleLogin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Signed In Content
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top header
                TopHeader(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToInsights = onNavigateToInsights,
                    onHamburgerClick = onNavigateToMenu
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group, // Could use a different icon like Flag or Star if available
                            contentDescription = "Missions",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Study Group",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Join global challenges and study with friends.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
    
                    // --- Global Mission Selection ---
                    Text(
                        text = "Global Event",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
    
                    uiState.globalMission?.let { mission ->
                         GlobalMissionCard(
                            mission = mission,
                            progress = uiState.globalMissionProgress,
                            onJoinClick = { socialViewModel.joinGlobalMission() }
                         )
                    } ?: run {
                        // Loading placeholder
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                    }
    
                    Spacer(modifier = Modifier.height(32.dp))
    
                    // --- Custom Missions ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Missions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        IconButton(onClick = { socialViewModel.showAddMissionDialog() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Mission",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.customMissions.isEmpty()) {
                         Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Create your first custom mission!",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        uiState.customMissions.forEach { mission ->
                            // Determine progress for custom mission (placeholder logic for now)
                            // In real app, we'd query specific mission progress
                            // For now we just show a generic progress or 0 if not tracked per-mission yet
                            CustomMissionItem(mission)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // --- Friends Section (Optional/Secondary) ---
                    if (uiState.friends.isNotEmpty()) {
                         Text(
                            text = "Studying Friends",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                             modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                         uiState.friends.forEach { friend ->
                            FriendItem(friend)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Add Mission Dialog
        if (uiState.showAddMissionDialog) {
            AlertDialog(
                onDismissRequest = { socialViewModel.hideAddMissionDialog() },
                title = { Text("New Custom Mission") },
                text = {
                    Column {
                        Text("Define your study goal:")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.newMissionTitle,
                            onValueChange = { socialViewModel.updateNewMissionTitle(it) },
                            singleLine = true,
                            label = { Text("Mission Title") },
                            placeholder = { Text("e.g. Master Calculus") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.newMissionTargetHours,
                            onValueChange = { socialViewModel.updateNewMissionTargetHours(it) },
                            singleLine = true,
                            label = { Text("Target Hours") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { socialViewModel.createCustomMission() }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { socialViewModel.hideAddMissionDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// CustomMissionItem moved to ui/components/CustomMissionItem.kt

@Composable
fun FriendItem(user: com.perseverance.pvc.data.SocialUser) {
    val isStudying = user.status == "STUDYING"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isStudying) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Setup simple avatar placeholder
            Box(
                modifier = Modifier.size(40.dp).background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Text(user.displayName.first().toString(), color = Color.White)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(user.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (isStudying) {
                    Text("Studying ${user.currentSubject}", color = Color(0xFF4CAF50))
                } else {
                    Text("Idle", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}


