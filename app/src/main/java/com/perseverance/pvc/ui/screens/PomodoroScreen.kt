package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.utils.ResponsiveTextSizes
import com.perseverance.pvc.ui.utils.ResponsiveSpacing
import com.perseverance.pvc.ui.utils.ResponsivePadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.perseverance.pvc.R
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.viewmodel.PomodoroViewModel
import com.perseverance.pvc.ui.viewmodel.SessionType

@Composable
fun PomodoroScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onTimerStateChanged: (Boolean) -> Unit = {},
    onViewModelCreated: (PomodoroViewModel) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PomodoroViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDurationDialog by remember { mutableStateOf(false) }
    
    // Notify parent about ViewModel creation (only once)
    androidx.compose.runtime.LaunchedEffect(viewModel) {
        onViewModelCreated(viewModel)
    }
    
    // Notify parent about timer state changes
    androidx.compose.runtime.LaunchedEffect(uiState.isPlaying) {
        onTimerStateChanged(uiState.isPlaying)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Simple background - no video needed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        
        // Semi-transparent overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.Black.copy(alpha = 0.3f)
                    else
                        Color.Transparent
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top header - only show when timer is not running or paused
            if (!uiState.isPlaying && !uiState.isPaused) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopHeader(
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToInsights = onNavigateToInsights,
                        onHamburgerClick = onNavigateToMenu
                    )
                }
            }
            // Main content with padding - centered vertically
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(ResponsivePadding.screen()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            
            // Status indicator (clickable)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { 
                    // Only allow subject selection during work sessions when timer is not running
                    if (uiState.currentSessionType == SessionType.WORK && !uiState.isPlaying && !uiState.isPaused) {
                        viewModel.showSubjectDialog() 
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.currentSessionType == SessionType.WORK) 
                                Color(0xFFFFD700) 
                            else 
                                Color(0xFF4CAF50) // Green for break
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.currentSessionType == SessionType.WORK) 
                        uiState.selectedSubject 
                    else 
                        "Break",
                    fontSize = ResponsiveTextSizes.subjectText().sp,
                    color = if (uiState.isPlaying || uiState.isPaused) 
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.small()))
            
            // Timer display with click to change duration
            Text(
                text = uiState.timeDisplay,
                fontSize = ResponsiveTextSizes.timerDisplay().sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    // Only allow changing duration when timer is not running or is completed
                    if (!uiState.isPlaying && !uiState.isPaused) {
                        showDurationDialog = true
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
            
            // Progress indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small())
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(ResponsiveSpacing.small())
                            .clip(CircleShape)
                            .background(
                                if (index < uiState.completedSessions) 
                                    Color(0xFF4CAF50) 
                                else 
                                    Color(0xFFE0E0E0)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.extraLarge()))
            
            // Cat illustration placeholder (simplified version)
            CatIllustration(isPlaying = uiState.isPlaying)
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            
            // Total study time display (green marked area)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ResponsivePadding.screen())
            ) {
                Text(
                    text = "Today's Total Study Time",
                    fontSize = ResponsiveTextSizes.labelText().sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(ResponsiveSpacing.small()))
                Text(
                    text = uiState.totalStudyTimeDisplay,
                    fontSize = ResponsiveTextSizes.totalStudyTime().sp,
                    color = Color(0xFF4CAF50),  // Green color
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(ResponsiveSpacing.large()))
            
            // Buttons
            if (uiState.isTimerCompleted) {
                // Show "I got it" button when timer is completed
                Button(
                    onClick = { viewModel.acknowledgeTimerCompletion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ResponsiveSpacing.extraLarge() + 10.dp)
                        .padding(horizontal = ResponsivePadding.screen() * 1.5f),
                    shape = RoundedCornerShape(ResponsiveSpacing.extraLarge() / 2 + 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "✓ I got it",
                        fontSize = ResponsiveTextSizes.buttonText().sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
                
                // Take a Break / Start Focus button (changes based on session type)
                Button(
                    onClick = { 
                        val activity = context as? ComponentActivity
                        viewModel.startBreakTimer(activity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ResponsiveSpacing.extraLarge() + 10.dp)
                        .padding(horizontal = ResponsivePadding.screen() * 1.5f),
                    shape = RoundedCornerShape(ResponsiveSpacing.extraLarge() / 2 + 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (uiState.currentSessionType == SessionType.WORK) "☕ Take a Break" else "▶ Start Focus",
                        fontSize = ResponsiveTextSizes.buttonText().sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (uiState.isPlaying || uiState.isPaused) {
                // Show Pause/Resume and Done while playing or paused
                Row(
                    modifier = Modifier
                        .padding(horizontal = ResponsivePadding.screen() * 2),
                    horizontalArrangement = Arrangement.spacedBy(ResponsiveSpacing.medium())
                ) {
                    // Pause/Resume Button
                    Button(
                        onClick = {
                            if (uiState.isPlaying) {
                                viewModel.pauseTimer()
                            } else {
                                val activity = context as? ComponentActivity
                                viewModel.startTimer(activity)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(ResponsiveSpacing.extraLarge() + 4.dp),
                        shape = RoundedCornerShape(ResponsiveSpacing.extraLarge() / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = ResponsivePadding.button(), vertical = ResponsiveSpacing.small())
                        ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Resume",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(ResponsiveSpacing.small()))
                        Text(
                            text = if (uiState.isPlaying) "Pause" else "Resume",
                            fontSize = ResponsiveTextSizes.buttonText().sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Done Button
                    Button(
                        onClick = { 
                            viewModel.completeSession()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(ResponsiveSpacing.extraLarge() + 4.dp),
                        shape = RoundedCornerShape(ResponsiveSpacing.extraLarge() / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = ResponsivePadding.button(), vertical = ResponsiveSpacing.small())
                    ) {
                        Text(
                            text = "✓ Done",
                            fontSize = ResponsiveTextSizes.buttonText().sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Show single Start Focus button when timer is not running
                Button(
                    onClick = { 
                        val activity = context as? ComponentActivity
                        viewModel.startTimer(activity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ResponsiveSpacing.extraLarge() + 10.dp)
                        .padding(horizontal = ResponsivePadding.screen() * 1.5f),
                    shape = RoundedCornerShape(ResponsiveSpacing.extraLarge() / 2 + 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "▶ Start Focus",
                        fontSize = ResponsiveTextSizes.buttonText().sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } // Close inner Column (centered content)
        
        // Dialogs rendered as overlays
        if (uiState.showSubjectDialog) {
            SubjectSelectionDialog(
                selectedSubject = uiState.selectedSubject,
                availableSubjects = uiState.availableSubjects,
                onSubjectSelected = { viewModel.selectSubject(it) },
                onAddNewSubject = { viewModel.addNewSubject(it) },
                onDismiss = { viewModel.hideSubjectDialog() }
            )
        }
        
        // Timer duration dialog
        if (showDurationDialog) {
            TimerDurationDialog(
                onDismiss = { showDurationDialog = false },
                onDurationSelected = { minutes ->
                    viewModel.updateTimerDurationFromHome(minutes)
                    showDurationDialog = false
                }
            )
        }
        } // Close outer Column
    } // Close main Box
}

@Composable
fun CatIllustration(isPlaying: Boolean) {
    // Study illustration - changes based on timer state
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (isPlaying) R.drawable.study else R.drawable.home
            ),
            contentDescription = if (isPlaying) "Studying" else "Ready to study",
            modifier = Modifier.size(180.dp)
            // No color filter - using original PNG colors
        )
    }
}

@Composable
fun SubjectSelectionDialog(
    selectedSubject: String,
    availableSubjects: List<String>,
    onSubjectSelected: (String) -> Unit,
    onAddNewSubject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsivePadding.dialog()),
            shape = RoundedCornerShape(ResponsiveSpacing.medium()),
            color = Color(0xFF2C2C2C)
        ) {
            Column(
                modifier = Modifier.padding(ResponsivePadding.dialog())
            ) {
                Text(
                    text = "Select Subject",
                    fontSize = ResponsiveTextSizes.dialogTitle().sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small())
                ) {
                    items(availableSubjects) { subject ->
                        SubjectItem(
                            subject = subject,
                            isSelected = subject == selectedSubject,
                            onClick = { onSubjectSelected(subject) }
                        )
                    }
                    
                    item {
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = ResponsiveSpacing.small()),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(ResponsiveSpacing.medium())
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(ResponsiveSpacing.small()))
                            Text(
                                text = "Add New Subject",
                                fontSize = ResponsiveTextSizes.dialogText().sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newSubject ->
                onAddNewSubject(newSubject)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubjectItem(
    subject: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(ResponsiveSpacing.medium()),
        color = if (isSelected) Color(0xFF6B5028) else Color(0xFF3C3C3C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveSpacing.medium()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject,
                fontSize = ResponsiveTextSizes.dialogText().sp,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(ResponsiveSpacing.medium() + 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsivePadding.dialog()),
            shape = RoundedCornerShape(ResponsiveSpacing.medium()),
            color = Color(0xFF2C2C2C)
        ) {
            Column(
                modifier = Modifier.padding(ResponsivePadding.dialog())
            ) {
                Text(
                    text = "Add New Subject",
                    fontSize = ResponsiveTextSizes.dialogTitle().sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
                )
                
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name", color = Color.Gray, fontSize = ResponsiveTextSizes.dialogText().sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ResponsiveSpacing.medium()),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel", fontSize = ResponsiveTextSizes.dialogText().sp)
                    }
                    
                    Spacer(modifier = Modifier.width(ResponsiveSpacing.small()))
                    
                    Button(
                        onClick = {
                            if (subjectName.isNotBlank()) {
                                onAdd(subjectName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        enabled = subjectName.isNotBlank()
                    ) {
                        Text("Add", fontSize = ResponsiveTextSizes.dialogText().sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TimerDurationDialog(
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    val durationOptions = listOf(5, 10, 50, 60)
    var showCustomDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsivePadding.dialog()),
            shape = RoundedCornerShape(ResponsiveSpacing.medium()),
            color = Color(0xFF2C2C2C)
        ) {
            Column(
                modifier = Modifier.padding(ResponsivePadding.dialog())
            ) {
                Text(
                    text = "Change Timer Duration",
                    fontSize = ResponsiveTextSizes.dialogTitle().sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveSpacing.small())
                ) {
                    items(durationOptions.size) { index ->
                        val duration = durationOptions[index]
                        Button(
                            onClick = { onDurationSelected(duration) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3C3C3C),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(ResponsiveSpacing.small())
                        ) {
                            Text(
                                text = "$duration minutes",
                                fontSize = ResponsiveTextSizes.dialogText().sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Custom option
                    item {
                        Button(
                            onClick = { showCustomDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(ResponsiveSpacing.small())
                        ) {
                            Text(
                                text = "Custom",
                                fontSize = ResponsiveTextSizes.dialogText().sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(ResponsiveSpacing.medium()))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = ResponsiveTextSizes.dialogText().sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // Custom duration input dialog
    if (showCustomDialog) {
        CustomDurationInputDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onDurationSelected(minutes)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomDurationInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var customMinutes by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    
    // Validate input
    LaunchedEffect(customMinutes) {
        val minutes = customMinutes.toIntOrNull()
        isValid = minutes != null && minutes > 0 && minutes <= 999
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsivePadding.dialog()),
            shape = RoundedCornerShape(ResponsiveSpacing.medium()),
            color = Color(0xFF2C2C2C)
        ) {
            Column(
                modifier = Modifier.padding(ResponsivePadding.dialog())
            ) {
                Text(
                    text = "Custom Timer Duration",
                    fontSize = ResponsiveTextSizes.dialogTitle().sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
                )
                
                Text(
                    text = "Enter timer duration in minutes (1-999):",
                    fontSize = ResponsiveTextSizes.dialogText().sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = ResponsiveSpacing.medium())
                )
                
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { customMinutes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ResponsiveSpacing.medium()),
                    placeholder = { Text("Enter minutes", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onConfirm(customMinutes.toInt()) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Timer")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PomodoroScreenPreview() {
    PerseverancePVCTheme {
        PomodoroScreen()
    }
}
