package com.perseverance.pvc.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perseverance.pvc.auth.GoogleAuthClient
import com.perseverance.pvc.auth.SignInResult
import com.perseverance.pvc.data.model.Group
import com.perseverance.pvc.data.model.Message
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    viewModel: GroupViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthClient = remember { GoogleAuthClient(context) }
    
    val user by viewModel.user.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val messages by viewModel.messages.collectAsState()

    // Google Sign In Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                val signInResult = googleAuthClient.signInWithIntent(result.data ?: return@launch)
                if (signInResult is SignInResult.Success) {
                    viewModel.signInWithGoogle(signInResult.idToken)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu
            )

            if (user == null) {
                // Not Logged In
                LoginContent(
                    onSignInClick = {
                        launcher.launch(googleAuthClient.getSignInIntent())
                    }
                )
            } else {
                // Logged In
                if (selectedGroupId == null) {
                    // No Group Selected
                    GroupSelectionContent(
                        groups = groups,
                        onGroupSelect = { groupId ->
                            viewModel.joinGroup(groupId)
                        },
                        onSignOut = { viewModel.signOut() }
                    )
                } else {
                    // Group Selected
                    val selectedGroup = groups.find { it.id == selectedGroupId }
                    if (selectedGroup != null) {
                        GroupDetailContent(
                            group = selectedGroup,
                            messages = messages,
                            onSendMessage = { content -> viewModel.sendMessage(content) },
                            onBack = { /* No back from group selection usually, but maybe helpful? Only if we allow changing groups easily. For now, selection is sticky per logic. */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginContent(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Access Study Groups",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Join a community to stay consistent",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSignInClick,
            modifier = Modifier.height(50.dp)
        ) {
            Text("Sign in with Google")
        }
    }
}

@Composable
fun GroupSelectionContent(
    groups: List<Group>,
    onGroupSelect: (String) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select a Group",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(groups) { group ->
                GroupCard(group = group, onClick = { onGroupSelect(group.id) })
            }
        }
        
        TextButton(onClick = onSignOut) {
            Text("Sign Out", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun GroupCard(group: Group, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (group.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun GroupDetailContent(
    group: Group,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
             Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (group.name == "The Launch") {
            ChatInterface(messages, onSendMessage)
        } else {
            // Static content for other groups
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (group.name == "Consistency Club") Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to ${group.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = group.description ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ChatInterface(messages: List<Message>, onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                // Basic message bubble
                // Ideally we check if it's me or other user to align left/right
                // For now, simple list
                Card(
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }
            ) {
                Icon(Icons.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
