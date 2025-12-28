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
import androidx.compose.ui.draw.rotate
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
        GroupMember("Tanjim Shakil", time = "02:41:41", isActive = true, isStudying = true),
        GroupMember("Nusrat Jahan", time = "01:12:39", isActive = true, isStudying = true),
        GroupMember("Ahmed Riaz", time = "00:58:43", isActive = true, isStudying = true),
        GroupMember("Farhana Rifa", time = "00:45:52", isActive = true, isStudying = true)
    )

    val allMembers = listOf(
        GroupMember("Leonor", time = "11:16:19", isActive = false), // Not studying (example)
        GroupMember("Abid Hasan", time = "10:41:16", isActive = true, isStudying = true),
        GroupMember("Jenifar Akter", time = "07:00:47", isActive = false),
        GroupMember("Sajjad Hossain", time = "05:42:23", isActive = true, isStudying = true),
        GroupMember("Mehedi Hasan", time = "04:53:47", isActive = false),
        GroupMember("Sumaiya Islam", time = "04:40:02", isActive = false),
        GroupMember("Rafiqul Islam", time = "04:35:42", isActive = true, isStudying = true),
        GroupMember("Tasnim Rahman", time = "04:12:30", isActive = true, isStudying = true),
        GroupMember("Karim Ullah", time = "03:12:30", isActive = false),
        GroupMember("Rahim Badsha", time = "02:12:30", isActive = true, isStudying = true),
        GroupMember("Ayesha Siddi..", time = "01:12:30", isActive = true, isStudying = true),
        GroupMember("User 12", time = "00:12:30", isActive = false),
    )

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
                                        .clip(CircleShape)
                                        .background(midNightGray) // Dark Gray Background
                                ) {
                                    Image(
                                        painter = painterResource(id = com.perseverance.pvc.R.drawable.study), // Always study icon for this section
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
                                             .size(60.dp) // Larger circle
                                             .clip(CircleShape)
                                             .background(midNightGray)
                                     ) {
                                          Image(
                                             painter = painterResource(
                                                 id = if (member.isStudying) com.perseverance.pvc.R.drawable.study else com.perseverance.pvc.R.drawable.home
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
                                         color = orangeAccent,
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

        // Overlay Header (Back & Notification)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White // White icon for better visibility on cover
                )
            }

            // Notification Icon with Badge
            Box {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.Notifications, 
                        contentDescription = "Notifications", 
                        tint = Color.White // White icon
                    )
                }
                Badge(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Text("3")
                }
            }
        }
    }
}
