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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    socialViewModel: SocialViewModel = viewModel(),
    pomodoroViewModel: com.perseverance.pvc.ui.viewmodel.PomodoroViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToMenu: () -> Unit,
    initialEditMode: Boolean = false
) {
    val socialUiState by socialViewModel.uiState.collectAsState()
    val pomodoroUiState by pomodoroViewModel.uiState.collectAsState()
    val isLight = isLightTheme()

    var isEditing by remember { mutableStateOf(initialEditMode) }

    // Initialize state from existing user data (for Edit View)
    var displayName by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.displayName ?: "") }
    var bio by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.bio ?: "") }
    var gender by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.gender ?: "") }
    var dateOfBirth by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.dateOfBirth ?: "") }
    var address by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.address ?: "") }
    var phoneNumber by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.phoneNumber ?: "") }
    var secondaryEmail by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.secondaryEmail ?: "") }
    var username by remember(socialUiState.currentUser) { mutableStateOf(socialUiState.currentUser?.username ?: "") }
    
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
                showBackButton = isEditing,
                onBackClick = { isEditing = false },
                title = if (isEditing) "Edit Profile" else "Profile"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isEditing) {
                // --- Edit Profile View ---
                if (!socialUiState.isSignedIn) {
                    // Not Logged In State (Same as before)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ... (Existing login prompt code)
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
                        // Profile Picture Picker
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
                                    model = selectedPhotoUri ?: socialUiState.currentUser?.photoUrl?.takeIf { !it.isNullOrBlank() } ?: "https://i.pravatar.cc/300", 
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
                                    icon = Icons.Default.AlternateEmail,
                                    supportingText = { Text("Must be unique", fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ProfileTextField(
                                    value = bio,
                                    onValueChange = { if (it.length <= 500) bio = it },
                                    label = "Bio",
                                    icon = Icons.Default.Info,
                                    maxLines = 5,
                                    supportingText = { 
                                        Text(
                                            text = "${bio.length}/500",
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                            fontSize = 11.sp
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Gender Selector
                                Text("Gender", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = gender.equals("Male", ignoreCase = true), onClick = { gender = "Male" })
                                    Text("Male", modifier = Modifier.clickable { gender = "Male" })
                                    Spacer(modifier = Modifier.width(16.dp))
                                    RadioButton(selected = gender.equals("Female", ignoreCase = true), onClick = { gender = "Female" })
                                    Text("Female", modifier = Modifier.clickable { gender = "Female" })
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Date of Birth Picker
                                var showDatePicker by remember { mutableStateOf(false) }
                                if (showDatePicker) {
                                    val datePickerState = rememberDatePickerState()
                                    DatePickerDialog(
                                        onDismissRequest = { showDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(millis))
                                                    dateOfBirth = date
                                                }
                                                showDatePicker = false
                                            }) { Text("OK") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = dateOfBirth,
                                        onValueChange = { }, // Read only, set by picker
                                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false, // Disable typing, enable click
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    // Transparent box to capture clicks over the disabled text field
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showDatePicker = true }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                 ProfileTextField(value = address, onValueChange = { address = it }, label = "Address", icon = Icons.Default.Home)
                                Spacer(modifier = Modifier.height(16.dp))
                                 ProfileTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone Number", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                                Spacer(modifier = Modifier.height(16.dp))
                                 ProfileTextField(value = secondaryEmail, onValueChange = { secondaryEmail = it }, label = "Secondary Email", icon = Icons.Default.Email, keyboardType = KeyboardType.Email)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Save Button
                        if (socialUiState.isLoading) {
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
                                    isEditing = false // Go back to dashboard on save
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
            } else {
                // --- Dashboard View (New) ---
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val user = socialUiState.currentUser
                    
                    // User Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF101929) // Dark blue background similar to design
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D45))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Image
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { isEditing = true } // Click to edit
                                ) {
                                    AsyncImage(
                                        model = user?.photoUrl?.takeIf { it.isNotBlank() } ?: "https://i.pravatar.cc/300",
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Guest User",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700) // Gold
                                    )
                                    Text(
                                        text = "PROFILE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFFFD700).copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Achievements Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "ACHIEVEMENTS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFD700)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Stats Row (Coins, Gems, Gifts)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatBadge("541", Icons.Default.MonetizationOn, Color(0xFFFFC107))
                                StatBadge("0", Icons.Default.Diamond, Color(0xFF03A9F4))
                                StatBadge("0", Icons.Default.CardGiftcard, Color(0xFFF44336))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Rank Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "UNRANKED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { 0.9f }, // Dummy progress
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = Color(0xFFFFD700),
                                    trackColor = Color(0xFF1E2D45),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "NO RANKS AVAILABLE",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Main Content Row (Statistics & Streak)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column (Statistics & Leaderboard)
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "STATISTICS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Text(
                                text = "LEADERBOARD\nPOSITION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("TIME: 4TH", color = Color.Gray, fontSize = 12.sp)
                            Text("ANKI: COMING SOON", color = Color.Gray, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "STUDY TIME",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val studyTime = pomodoroUiState.totalStudyTimeDisplay
                            Text("DAILY: $studyTime", color = Color.Gray, fontSize = 12.sp)
                            Text("WEEKLY: 0.0", color = Color.Gray, fontSize = 12.sp)
                            Text("MONTHLY: 04:10", color = Color.Gray, fontSize = 12.sp) // Mocked
                            Text("ALL TIME: 04:10", color = Color.Gray, fontSize = 12.sp) // Mocked
                        }
                        
                        // Right Column (Study Streak)
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "STUDY STREAK",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Text(
                                text = "DECEMBER: 4 hours", // Mocked
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF03A9F4)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mock Calendar
                            SimpleCalendar()
                        }
                    }
                }               
            }
        }
    }
}

@Composable
fun StatBadge(value: String, icon: ImageVector, color: Color) {
    Surface(
        color = Color(0xFF1E2D45),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SimpleCalendar() {
    Column {
        val days = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(text = day, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mock dates
        val dates = (1..31).toList()
        val chunkedDates = dates.chunked(7)
        val currentDay = 25 // Mock
        val studiedDays = listOf(25, 27) // Mock
        
        chunkedDates.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { date ->
                    val isStudied = date in studiedDays
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isStudied) Color(0xFF03A9F4) else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = date.toString(),
                            color = if (isStudied) Color.White else Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
                // Fill remaining spaces in the last row
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = maxLines,
        supportingText = supportingText,
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
