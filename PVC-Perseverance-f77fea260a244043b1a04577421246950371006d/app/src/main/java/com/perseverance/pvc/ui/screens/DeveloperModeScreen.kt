package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.viewmodel.StudyViewModel
import com.perseverance.pvc.data.StudyRepository
import androidx.compose.runtime.collectAsState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DeveloperModeScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val studyViewModel: StudyViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    // Get the repository to fetch real subjects
    val repository = StudyRepository(context)
    val subjects by repository.getSavedSubjects().collectAsState(initial = emptyList())

    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }
    var subject by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top header with back button
        TopHeader(
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToInsights = onNavigateToInsights,
            onBackClick = onBackClick,
            showBackButton = true
        )

        // Title
        Text(
            text = "Developer Mode",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
        )

        // Description
        Text(
            text = "Add study time manually for testing purposes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        )

        // Main content with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date selection section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Study Date",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    // Date selection button
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Select Date",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatDateForDisplay(selectedDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Time input section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Study Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours input
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Hours",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = hours,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                        hours = it
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Minutes input
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Minutes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = minutes,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                        minutes = it
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }

            // Subject selection section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    // Subject dropdown
                    var expanded by remember { mutableStateOf(false) }
                    
                    if (subjects.isEmpty()) {
                        // Show message when no subjects are available
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "No subjects available. Please add subjects in the Pomodoro timer first.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                placeholder = { Text("Select a subject") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                subjects.forEach { subjectOption ->
                                    DropdownMenuItem(
                                        text = { Text(subjectOption) },
                                        onClick = {
                                            subject = subjectOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add time button
            Button(
                enabled = subjects.isNotEmpty(),
                onClick = {
                    try {
                        val hoursInt = hours.toIntOrNull() ?: 0
                        val minutesInt = minutes.toIntOrNull() ?: 0
                        
                        if (subjects.isEmpty()) {
                            errorMessage = "No subjects available. Please add subjects in the Pomodoro timer first."
                            showErrorDialog = true
                            return@Button
                        }
                        
                        if (subject.isEmpty()) {
                            errorMessage = "Please select a subject"
                            showErrorDialog = true
                            return@Button
                        }
                        
                        if (hoursInt == 0 && minutesInt == 0) {
                            errorMessage = "Please enter a valid time (at least 1 minute)"
                            showErrorDialog = true
                            return@Button
                        }
                        
                        val totalMinutes = hoursInt * 60 + minutesInt
                        val currentTime = LocalTime.now()
                        
                        // Add the study time
                        studyViewModel.addManualStudyTime(
                            date = selectedDate,
                            subject = subject,
                            durationMinutes = totalMinutes,
                            startTime = currentTime
                        )
                        
                        showSuccessDialog = true
                        
                        // Reset form
                        hours = "0"
                        minutes = "0"
                        subject = ""
                        
                    } catch (e: Exception) {
                        errorMessage = "Error adding study time: ${e.message}"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Study Time",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Study Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick add buttons
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("15m", "30m", "1h", "2h").forEach { timeLabel ->
                    val (labelHours, labelMinutes) = when (timeLabel) {
                        "15m" -> 0 to 15
                        "30m" -> 0 to 30
                        "1h" -> 1 to 0
                        "2h" -> 2 to 0
                        else -> 0 to 0
                    }
                    
                    OutlinedButton(
                        onClick = {
                            hours = labelHours.toString()
                            minutes = labelMinutes.toString()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(timeLabel)
                    }
                }
            }

            // Spacer for bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text("Success!")
            },
            text = {
                Text("Study time has been added successfully.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// Date formatting function
private fun formatDateForDisplay(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return date.format(formatter)
}

// Date picker dialog composable
@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf(selectedDate) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Study Date",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose the date for this study session:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Simple date input fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Year",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = date.year.toString(),
                            onValueChange = { yearStr ->
                                yearStr.toIntOrNull()?.let { year ->
                                    if (year >= 2020 && year <= 2030) {
                                        date = date.withYear(year)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    // Month input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = date.monthValue.toString(),
                            onValueChange = { monthStr ->
                                monthStr.toIntOrNull()?.let { month ->
                                    if (month >= 1 && month <= 12) {
                                        date = date.withMonth(month)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    // Day input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = date.dayOfMonth.toString(),
                            onValueChange = { dayStr ->
                                dayStr.toIntOrNull()?.let { day ->
                                    if (day >= 1 && day <= 31) {
                                        date = date.withDayOfMonth(day)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected(date) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
