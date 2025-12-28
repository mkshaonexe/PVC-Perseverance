package com.perseverance.pvc.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.viewmodel.SocialViewModel

@Composable
fun EditProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onBackClick: () -> Unit = {},
    isProfileIncomplete: Boolean = false,
    socialViewModel: SocialViewModel = viewModel()
) {
    val uiState by socialViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    // State for form fields
    var displayName by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var username by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var gender by remember(currentUser) { mutableStateOf(currentUser?.gender ?: "") }
    var dateOfBirth by remember(currentUser) { mutableStateOf(currentUser?.dateOfBirth ?: "") }
    var address by remember(currentUser) { mutableStateOf(currentUser?.address ?: "") }
    var phoneNumber by remember(currentUser) { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var secondaryEmail by remember(currentUser) { mutableStateOf(currentUser?.secondaryEmail ?: "") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    var showGenderDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showIncompleteWarning by remember { mutableStateOf(false) }
    
    // Check if profile is complete
    val hasName = displayName.isNotEmpty()
    val hasPhoto = selectedPhotoUri != null || currentUser?.photoUrl?.isNotEmpty() == true
    val isComplete = hasName && hasPhoto
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            // TODO: Upload to storage and update avatar_url
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu,
                showBackButton = true,
                onBackClick = {
                    if (isProfileIncomplete && !isComplete) {
                        showIncompleteWarning = true
                    } else {
                        onBackClick()
                    }
                },
                title = if (isProfileIncomplete) "Complete Your Profile" else "Edit Profile"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mandatory completion message
            if (isProfileIncomplete) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Please set your name and profile picture to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Profile Photo with Edit Button
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                ) {
                    AsyncImage(
                        model = selectedPhotoUri ?: currentUser?.photoUrl ?: "https://i.pravatar.cc/300",
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Yellow Edit Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700))
                        .clickable {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Photo",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap to change photo",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Full Name Field
            EditProfileTextField(
                icon = Icons.Default.Person,
                label = "Full Name",
                value = displayName,
                onValueChange = { displayName = it },
                iconTint = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username Field
            EditProfileTextField(
                icon = Icons.Default.AlternateEmail,
                label = "Username",
                value = username,
                onValueChange = { username = it },
                iconTint = Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bio Field
            EditProfileTextField(
                icon = Icons.Default.Info,
                label = "Bio",
                value = bio,
                onValueChange = { bio = it },
                iconTint = Color(0xFFFFD700),
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gender Field (Clickable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showGenderDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wc,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (gender.isEmpty()) "Gender" else gender,
                        color = if (gender.isEmpty()) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date of Birth Field
            EditProfileTextField(
                icon = Icons.Default.CalendarToday,
                label = "Date of Birth (YYYY-MM-DD)",
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                iconTint = Color(0xFFFFD700),
                placeholder = "YYYY-MM-DD"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone Number Field
            EditProfileTextField(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                iconTint = Color(0xFF4CAF50),
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Secondary Email Field
            EditProfileTextField(
                icon = Icons.Default.Email,
                label = "Secondary Email",
                value = secondaryEmail,
                onValueChange = { secondaryEmail = it },
                iconTint = Color(0xFF4CAF50),
                keyboardType = KeyboardType.Email
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Address Field
            EditProfileTextField(
                icon = Icons.Default.Place,
                label = "Address",
                value = address,
                onValueChange = { address = it },
                iconTint = Color(0xFF4CAF50),
                singleLine = false,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (isProfileIncomplete && !isComplete) {
                        showIncompleteWarning = true
                        return@Button
                    }
                    
                    isSaving = true
                    // Save locally
                    socialViewModel.updateFullProfile(
                        displayName = displayName,
                        bio = bio,
                        gender = gender,
                        dateOfBirth = dateOfBirth,
                        address = address,
                        phoneNumber = phoneNumber,
                        secondaryEmail = secondaryEmail,
                        username = username,
                        imageUri = selectedPhotoUri
                    )
                    // Navigate back after save completes
                    if (!isProfileIncomplete) {
                        onBackClick()
                    } else {
                        isSaving = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving && (!isProfileIncomplete || isComplete)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            OutlinedButton(
                onClick = {
                    socialViewModel.signOut()
                    onBackClick() // Simple navigation back, the Auth listener will handle the rest
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Gender Selection Dialog
    if (showGenderDialog) {
        AlertDialog(
            onDismissRequest = { showGenderDialog = false },
            title = { Text("Select Gender") },
            text = {
                Column {
                    listOf("Male", "Female", "Other", "Prefer not to say").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    gender = option
                                    showGenderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gender == option,
                                onClick = {
                                    gender = option
                                    showGenderDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGenderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Incomplete Profile Warning Dialog
    if (showIncompleteWarning) {
        AlertDialog(
            onDismissRequest = { showIncompleteWarning = false },
            title = { Text("Profile Incomplete") },
            text = {
                Text("Please set both your name and profile picture to continue. These are required fields.")
            },
            confirmButton = {
                TextButton(onClick = { showIncompleteWarning = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun EditProfileTextField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    iconTint: Color = Color(0xFFFFD700),
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder.ifEmpty { label }) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType
        )
    )
}
