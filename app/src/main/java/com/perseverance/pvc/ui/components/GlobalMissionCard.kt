package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.data.Mission

@Composable
fun GlobalMissionCard(
    mission: Mission,
    progress: Long,
    onJoinClick: () -> Unit
) {
    val progressHours = progress / 3600.0
    val progressPercent = (progress.toDouble() / mission.targetSeconds).coerceIn(0.0, 1.0)
    
    // "Offline but Online" feel: Show large participant count
    val participants = java.text.NumberFormat.getNumberInstance().format(mission.participantCount)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark card
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                 Text(
                    text = "LIMITED TIME EVENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    letterSpacing = 1.sp
                )
                
                // Live indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = mission.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
             Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mission.description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (mission.isJoined) {
                // Progress UI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format("%.1f", progressHours),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "/ ${mission.targetSeconds / 3600} hrs",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progressPercent.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFFFFD700),
                    trackColor = Color.White.copy(alpha = 0.1f),
                   strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$participants participants fighting now",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Join UI
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ACCEPT CHALLENGE", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                 Text(
                    text = "Join $participants others in this challenge",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
