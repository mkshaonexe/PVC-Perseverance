package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GroupMember(
    val name: String,
    val imageUrl: String? = null,
    val time: String,
    val isActive: Boolean = false,
    val isStudying: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    onNavigateToSettings: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Mock Data
    val activeMembers = listOf(
        GroupMember("Tanjim$&", time = "12:41:41", isActive = true, isStudying = true),
        GroupMember("Tabasum...", time = "12:12:39", isStudying = false),
        GroupMember("TL+HMED", time = "12:08:43", isActive = true, isStudying = true),
        GroupMember("FA RIFAZ", time = "12:00:52", isActive = true, isStudying = true)
    )

    val allMembers = listOf(
        GroupMember("Leonor", time = "11:16:19", isActive = true),
        GroupMember("abiiiiiida", time = "10:41:16", isActive = true),
        GroupMember("jenifar", time = "7:00:47", isActive = false),
        GroupMember("Sajjatul Ah...", time = "5:42:23", isActive = true),
        GroupMember("✨MAVERI...", time = "4:53:47", isActive = false),
        GroupMember("sumaiya", time = "4:40:02", isActive = false),
        GroupMember("Rurah", time = "4:35:42", isActive = true),
        GroupMember("al tasin", time = "4:12:30", isActive = true),
        GroupMember("User 9", time = "3:12:30", isActive = true),
        GroupMember("User 10", time = "2:12:30", isActive = true),
        GroupMember("User 11", time = "1:12:30", isActive = true),
        GroupMember("User 12", time = "0:12:30", isActive = true),
    )

    // Using MaterialTheme.colorScheme.background ensures it matches the app's current theme (Midnight/Dark/Light)
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryTextColor
                        )
                    }
                },
                actions = {
                    // Notification Icon with Badge
                    Box {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Filled.Notifications, 
                                contentDescription = "Notifications", 
                                tint = primaryTextColor
                            )
                        }
                        Badge(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                            Text("3")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor, // Match background
                    navigationIconContentColor = primaryTextColor,
                    actionIconContentColor = primaryTextColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            // 1. Cover Photo
            item {
                Image(
                    painter = painterResource(id = com.perseverance.pvc.R.drawable.group_cover),
                    contentDescription = "Group Cover Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Adjusted height for better visibility
                )
            }

            // 2. Group by Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1565C0)) // Slightly lighter blue or distinct color
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Group by MK Shaon",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
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
                            text = "College Student Community",
                            style = MaterialTheme.typography.headlineSmall,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = primaryTextColor
                        )
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
                            text = "Public group • 1.2K members",
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
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activeMembers.size} members",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                             contentDescription = null,
                             tint = secondaryTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(activeMembers) { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray) // Placeholder
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_edit), // Placeholder Icon
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member.name,
                                    color = if (member.isActive) accentColor else secondaryTextColor,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = member.time,
                                    color = if (member.isActive) accentColor else secondaryTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))
            }

            // 5. Grid/List of Other Members
            // We use a FlowRow equivalent or just rows of 4 items for grid-like appearance in LazyColumn
            item {
                 Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                             .size(50.dp)
                                             .clip(CircleShape)
                                             .background(Color.DarkGray)
                                     ) {
                                          Icon(
                                             painter = painterResource(id = android.R.drawable.ic_menu_view), 
                                             contentDescription = null,
                                             tint = Color.White,
                                             modifier = Modifier.align(Alignment.Center)
                                         )
                                     }
                                     Spacer(modifier = Modifier.height(8.dp))
                                     Text(
                                         text = member.name,
                                         color = if (member.isActive) accentColor else secondaryTextColor,
                                         fontSize = 12.sp,
                                         textAlign = TextAlign.Center,
                                         maxLines = 1,
                                         modifier = Modifier.fillMaxWidth()
                                     )
                                     Text(
                                         text = member.time,
                                         color = if (member.isActive) accentColor else secondaryTextColor,
                                         fontSize = 12.sp,
                                         textAlign = TextAlign.Center,
                                         modifier = Modifier.fillMaxWidth()
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
}
