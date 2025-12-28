package com.perseverance.pvc.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.viewmodel.SocialViewModel
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import com.perseverance.pvc.ui.theme.isLightTheme

@Composable
fun ProfileScreen(
    socialViewModel: SocialViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToMenu: () -> Unit
) {
    val uiState by socialViewModel.uiState.collectAsState()
    val isLight = isLightTheme()
    
    // Initialize state from existing user data when available
    var displayName by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.displayName ?: "") }
    var bio by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.bio ?: "") }
    var gender by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.gender ?: "") }
    var dateOfBirth by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.dateOfBirth ?: "") }
    var address by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.address ?: "") }
    var phoneNumber by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.phoneNumber ?: "") }
    var secondaryEmail by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.secondaryEmail ?: "") }
    var username by remember(uiState.currentUser) { mutableStateOf(uiState.currentUser?.username ?: "") }
    
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu,
                showBackButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!uiState.isSignedIn) {
                // Not Logged In State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Sign in to Edit Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Connect with Google to save your profile information and sync across devices.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { socialViewModel.performGoogleLogin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Logged In - Edit Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp)
                    )

                    // Profile Picture
                    Box(modifier = Modifier.clickable { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = selectedPhotoUri ?: uiState.currentUser?.photoUrl?.takeIf { !it.isNullOrBlank() } ?: "https://i.pravatar.cc/300", 
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(8.dp)
                        ) {
                             Icon(Icons.Default.Edit, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Text(
                        text = "Tap to change photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                    // Form Fields
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = glassBorder(isLight),
                        elevation = glassElevation(isLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = "Full Name",
                                icon = Icons.Default.Person
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = "Username",
                                icon = Icons.Default.AlternateEmail
                            )
                             Spacer(modifier = Modifier.height(16.dp))
                            ProfileTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = "Bio",
                                icon = Icons.Default.Info,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = "Gender",
                                icon = Icons.Default.Male
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                             ProfileTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = "Date of Birth (YYYY-MM-DD)",
                                icon = Icons.Default.CalendarToday,
                                keyboardType = KeyboardType.Number
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                             ProfileTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = "Address",
                                icon = Icons.Default.Home
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                             ProfileTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = "Phone Number",
                                icon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                             ProfileTextField(
                                value = secondaryEmail,
                                onValueChange = { secondaryEmail = it },
                                label = "Secondary Email",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(
                            onClick = {
                                socialViewModel.updateFullProfile(
                                    displayName = displayName,
                                    imageUri = selectedPhotoUri,
                                    bio = bio,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    phoneNumber = phoneNumber,
                                    secondaryEmail = secondaryEmail,
                                    username = username
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save All Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Logout Option
                     TextButton(
                        onClick = { socialViewModel.signOut() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out")
                    }
                     Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}
