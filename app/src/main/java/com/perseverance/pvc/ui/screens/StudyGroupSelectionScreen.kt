package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.viewmodel.AuthViewModel

@Composable
fun StudyGroupSelectionScreen(
    authViewModel: AuthViewModel,
    onGroupSelected: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    // We could use this to store the selection to Supabase later
    // val scope = rememberCoroutineScope() 
    
    val groups = listOf(
        "Consistency Club",
        "Last Option",
        "Public Library"
    )
    
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Choose Your Study Group",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Select a group to join for your study journey.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                groups.forEach { groupName ->
                    StudyGroupCard(
                        groupName = groupName,
                        isSelected = selectedGroup == groupName,
                        onClick = { selectedGroup = groupName }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        selectedGroup?.let { group ->
                            // Save selection to Supabase
                            authViewModel.updateStudyGroup(group)
                            onGroupSelected(group)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedGroup != null,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StudyGroupCard(
    groupName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            null
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = groupName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
