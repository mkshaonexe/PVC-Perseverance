package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.components.TopHeader

// Define Midnight Theme Colors - Hardcoded based on reference/request for this screen
val MidnightBackground = Color(0xFF000000) // Deep Black
val MidnightPrimary = Color(0xFFFF6F00) // Orange Accent
val MidnightSurface = Color(0xFF1E1E1E) // Dark Gray for cards
val MidnightTextPrimary = Color(0xFFFFFFFF)
val MidnightTextSecondary = Color(0xFFB0B0B0)

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
        GroupMember("Tabasum...", time = "12:12:39", isStudying = false), // Not studying actively in list? Or just idle.
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
    )

    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White // Explicit white for midnight theme
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                    // Using a badge for notification count if possible, simple icon for now
                     Box {
                        IconButton(onClick = {}) {
                             Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                         Badge(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                             Text("3")
                         }
                     }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D47A1) // Blue header from reference image
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MidnightBackground)
        ) {
            // "Studying 4 members" section (Dark Section)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Text(
                         text = "Studying",
                         color = Color.Gray,
                         fontSize = 14.sp
                     )
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                         text = "4 members",
                         color = MidnightPrimary,
                         fontSize = 14.sp
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
                            // Avatar Placeholder - Using Icon
                             Box(
                                modifier = Modifier
                                    .size(50.dp)
                             ) {
                                 Icon(
                                     painter = painterResource(id = android.R.drawable.ic_menu_edit), // Placeholder
                                     contentDescription = null,
                                     tint = MidnightPrimary,
                                     modifier = Modifier.size(50.dp)
                                 )
                                 // Online indicator?
                             }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = member.name,
                                color = if (member.isActive) MidnightPrimary else Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Text(
                                text = member.time,
                                color = if (member.isActive) MidnightPrimary else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // "Group by MK Shaon" Blue Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D47A1))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                 Text(
                     text = "Group by MK Shaon",
                     color = Color.White,
                     fontWeight = FontWeight.Bold,
                     fontSize = 12.sp
                 )
            }

            // White/Light Content Area (As per reference image lower half)
            // Wait, reference shows white background for the lower list. 
            // User asked for "Midnight Theme" INSIDE the group.
            // Reference image has mixed: Top is dark/black, middle bar is blue, bottom is white.
            // Requirement: "use the app theme midnight theme on the inside group... that is shdul loook liek that"
            // Usually "Midnight" implies fully dark. But "look like that" implies mimicking the colors.
            // If I look at the reference closely, the top part is black background with orange icons.
            // The bottom part is white background with orange icons.
            // User said: "logic is simlge inside group shwo liek the grinven referace image"
            // AND "use the app theme midnight theme on the inside group"
            
            // If I use "Midnight Theme", everything should be dark.
            // The reference image seems to be from an app that HAS a dark mode header but light content? Or it's a specific design.
            // Given "Midnight theme" instruction, I will make the bottom part DARK as well, to be consistent with "Midnight".
            // If I make it white, it's not Midnight theme.
            // I will use MidnightBackground (Black) for the bottom part too, but keep the structure.

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MidnightBackground) // Enforcing Midnight Theme
                    .padding(16.dp)
            ) {
                item {
                    // Group Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "College Student Community",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MidnightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MidnightTextPrimary
                        )
                    }
                    
                    // Public group • 1.2K members
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                             imageVector = Icons.Filled.Public, // Need to make sure Public exists or use generic
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Studying 9 members
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Studying",
                            color = MidnightTextPrimary
                        )
                         Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "9 members",
                            color = MidnightPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Target Icon placeholder
                         Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Grid of members
                // We can't put `LazyVerticalGrid` inside a `LazyColumn` directly without fixed height.
                // Better to simple loop and create rows or use FlowRow (if available) or calculate rows.
                // Or just use `item` with a `FlowRow` like layout.
                // Since `LazyVerticalGrid` is better for grids, let's use a workaround or nested scrolling (not recommended).
                // Actually, `LazyColumn` is the root. I can use `gridItems` inside it if I simply change the root.
                // But I have a header.
                // Let's use `LazyVerticalGrid` as the MAIN container, and put headers as `item(span = { GridItemSpan(maxLineSpan) })`.
            }
        }
    }
}

// Re-writing with LazyVerticalGrid as root to handle mixed content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreenRevised(
    onNavigateToSettings: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val activeMembers = listOf(
        GroupMember("Tanjim$&", time = "12:41:41", isActive = true, isStudying = true),
        GroupMember("Tabasum...", time = "12:12:39", isStudying = false),
        GroupMember("TL+HMED", time = "12:08:43", isActive = true, isStudying = true),
        GroupMember("FA RIFAZ", time = "12:00:52", isActive = true, isStudying = true)
    )

    val gridMembers = listOf(
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

    Scaffold(
        containerColor = MidnightBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                         Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1) // Blue header
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MidnightBackground),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header Section: Studying Members (Horizontal List)
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(16.dp)
                ) {
                     Row(
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text(
                             text = "Studying",
                             color = Color.Gray,
                             fontSize = 14.sp
                         )
                         Spacer(modifier = Modifier.width(4.dp))
                         Text(
                             text = "4 members",
                             color = MidnightPrimary,
                             fontSize = 14.sp
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
                                        .background(Color.Transparent)
                                 ) {
                                     // Placeholder for "Studying" icon
                                     Icon(
                                         painter = painterResource(id = android.R.drawable.ic_menu_today), // Generic
                                         contentDescription = null,
                                         tint = MidnightPrimary,
                                         modifier = Modifier.size(50.dp)
                                     )
                                 }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member.name,
                                    color = if (member.isActive) MidnightPrimary else Color.Gray,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = member.time,
                                    color = if (member.isActive) MidnightPrimary else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Blue Bar "Group by..."
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D47A1))
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
            
            // Group Info Section
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
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
                            color = MidnightTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MidnightTextPrimary
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Globe Icon if available, else generic
                        Icon(
                             imageVector = Icons.Filled.Public, // Ensure using Filled
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Studying",
                            color = MidnightTextPrimary
                        )
                         Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "9 members",
                            color = MidnightPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Grid Items
            items(gridMembers) { member ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    // Start of Icon
                     Box(
                        modifier = Modifier
                            .size(50.dp)
                     ) {
                         Icon(
                             painter = painterResource(id = android.R.drawable.ic_menu_view), // Using standard android resource as placeholder
                             contentDescription = null,
                             tint = if (member.isActive) MidnightPrimary else Color.Gray,
                             modifier = Modifier.size(50.dp)
                         )
                     }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = member.name,
                        color = if (member.isActive) MidnightPrimary else Color.Gray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = member.time,
                        color = if (member.isActive) MidnightPrimary else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                         modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
