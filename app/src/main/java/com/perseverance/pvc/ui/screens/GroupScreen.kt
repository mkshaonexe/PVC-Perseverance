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
import androidx.compose.material3.*
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GroupScreen(
    socialViewModel: com.perseverance.pvc.ui.viewmodel.SocialViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    val uiState by socialViewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    // Launcher for Google Sign In
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            scope.launch {
                try {
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                    
                    val signInResult = com.perseverance.pvc.utils.SignInResult(
                        data = authResult.user?.let { user ->
                            com.perseverance.pvc.utils.UserData(
                                userId = user.uid,
                                username = user.displayName,
                                profilePictureUrl = user.photoUrl?.toString(),
                                email = user.email
                            )
                        },
                        errorMessage = null
                    )
                    socialViewModel.onSignInResult(signInResult)
                } catch (e: Exception) {
                     socialViewModel.onSignInResult(com.perseverance.pvc.utils.SignInResult(null, e.message))
                }
            }
        }
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
        
        // Content
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
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Group",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Study Friends",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                if (!uiState.isSignedIn) {
                    // Sign In UI
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                         border = com.perseverance.pvc.ui.theme.glassBorder(MaterialTheme.colorScheme.background.luminance() >= 0.5f),
                         elevation = com.perseverance.pvc.ui.theme.glassElevation(MaterialTheme.colorScheme.background.luminance() >= 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Connect with Friends",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Sign in with Google to see when your friends are studying.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                     val intent = socialViewModel.getSignInIntent()
                                     launcher.launch(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                            ) {
                                Text("Sign In with Google")
                            }
                        }
                    }
                } else {
                    // Logged In UI
                    
                    // Add Friend Button
                    Button(
                        onClick = { socialViewModel.showAddFriendDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Friend (${uiState.friends.size}/10)")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.friends.isEmpty()) {
                        Text(
                            "No friends yet. Add someone to see their status!",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        uiState.friends.forEach { friend ->
                            FriendItem(friend)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        if (uiState.showAddFriendDialog) {
            AlertDialog(
                onDismissRequest = { socialViewModel.hideAddFriendDialog() },
                title = { Text("Add Friend") },
                text = {
                    Column {
                        Text("Enter your friend's email address:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.addFriendEmail,
                            onValueChange = { socialViewModel.updateAddFriendEmail(it) },
                            singleLine = true,
                            label = { Text("Email") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { socialViewModel.sendFriendRequest() }) {
                        Text("Send Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { socialViewModel.hideAddFriendDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

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

@Composable
fun BenefitItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

