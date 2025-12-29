package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.perseverance.pvc.ui.viewmodel.SocialViewModel

import com.perseverance.pvc.ui.viewmodel.GroupMemberUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    socialViewModel: SocialViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by socialViewModel.uiState.collectAsState()
    val selectedGroup = uiState.selectedGroup
    val groupMembers = uiState.groupMembers

    // Filter members
    val activeMembers = groupMembers.filter { it.isStudying }
    val allMembers = groupMembers // Show everyone in the bottom grid, or maybe filter? 
    // The previous mock data had disjoint sets roughly, but "Abid" was in allMembers and was studying.
    // So allMembers likely implies "All Members" list.


    // Colors from Reference
    val backgroundColor = MaterialTheme.colorScheme.background // Black/Midnight
    val midNightGray = Color(0xFF2A2A2A) // Dark gray for circle backgrounds
    val orangeAccent = Color(0xFFFF9100) // Orange for text
    val blueBanner = Color(0xFF1976D2) // Blue for group banner
    val primaryTextColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Cover Photo (Starts at top)
            item {
                Image(
                    painter = painterResource(id = com.perseverance.pvc.R.drawable.group_cover),
                    contentDescription = "Group Cover Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Maintain user-defined height
                )
            }

            // 2. Group by Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(blueBanner)
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Group by MK Shaon",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // 3. Group Info (Name, Public, Members Count)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedGroup?.name ?: "College Student Community",
                            style = MaterialTheme.typography.headlineSmall,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (selectedGroup != null && !uiState.hasJoinedCurrentGroup) {
                             Button(
                                onClick = { 
                                    if (!uiState.isLoading) {
                                        socialViewModel.joinGroup(selectedGroup.id) 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.padding(start = 8.dp)
                             ) {
                                if (uiState.isLoading) {
                                     CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                     )
                                } else {
                                     Text("Join", fontWeight = FontWeight.Bold)
                                }
                             }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = primaryTextColor
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Public group • ${selectedGroup?.memberCount ?: "1.2K"} members",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 4. Studying Members Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Studying",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${activeMembers.size} members",
                            color = orangeAccent, // Orange text
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                             contentDescription = null,
                             tint = Color.Gray, // Light gray icon
                             modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(activeMembers) { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        // Removed background(midNightGray) and clip as requested
                                ) {
                                    Image(
                                        painter = painterResource(id = if (member.avatarResId != 0) member.avatarResId else com.perseverance.pvc.R.drawable.study), // Always study icon for this section
                                        contentDescription = "Studying",
                                        modifier = Modifier
                                            .size(40.dp) // Adjusted size for PNG
                                            .align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = member.name,
                                    color = orangeAccent,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = member.time,
                                    color = orangeAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))
            }

            // 5. Grid/List of Other Members
            item {
                 Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                     if (uiState.isLoadingMembers) {
                         // Skeleton Grid - Show 2 rows of skeletons
                         repeat(2) {
                             Row(
                                 modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                 horizontalArrangement = Arrangement.SpaceBetween
                             ) {
                                 repeat(4) {
                                     com.perseverance.pvc.ui.components.GroupMemberSkeleton()
                                 }
                             }
                         }
                     } else {
                         val chunks = allMembers.chunked(4)
                         chunks.forEach { rowMembers ->
                             Row(
                                 modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                 horizontalArrangement = Arrangement.SpaceBetween
                             ) {
                                 rowMembers.forEach { member ->
                                     Column(
                                         horizontalAlignment = Alignment.CenterHorizontally,
                                         modifier = Modifier.width(70.dp) // Fixed width for alignment
                                     ) {
                                         Box(
                                             modifier = Modifier
                                                 .size(60.dp)
                                                 // Removed background(midNightGray) and clip as requested
                                         ) {
                                             Image(
                                                 painter = painterResource(
                                                     id = if (member.avatarResId != 0) member.avatarResId else if (member.isStudying) com.perseverance.pvc.R.drawable.study else com.perseverance.pvc.R.drawable.home
                                                 ),
                                                 contentDescription = if (member.isStudying) "Studying" else "Not Studying",
                                                 modifier = Modifier
                                                     .size(40.dp)
                                                     .align(Alignment.Center)
                                             )
                                         }
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Text(
                                             text = member.name,
                                             color = orangeAccent,
                                             fontSize = 13.sp,
                                             textAlign = TextAlign.Center,
                                             maxLines = 1,
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                         Text(
                                             text = member.time,
                                             color = if (member.isStudying) orangeAccent else Color.Gray, // Orange if studying, Ash/Gray if not
                                             fontSize = 13.sp,
                                             textAlign = TextAlign.Center,
                                             modifier = Modifier.fillMaxWidth(),
                                             fontWeight = FontWeight.Bold
                                         )
                                     }
                                 }
                                 // Fill empty spots if last row is incomplete
                                 repeat(4 - rowMembers.size) {
                                     Spacer(modifier = Modifier.width(70.dp))
                                 }
                             }
                         }
                     }
                 }
            }
        }

        // Overlay Header (Back & Notification)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp) // Aligns with the "red box" area
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Notification Icon with Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .clickable { /* Handle Notifications */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications, 
                    contentDescription = "Notifications", 
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                // Red Dot Indicator/Badge
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .padding(2.dp)
                )
            }
        }
    }
}
