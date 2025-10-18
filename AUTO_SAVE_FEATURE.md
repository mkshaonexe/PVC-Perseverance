# Auto-Save Feature Implementation

## Overview
The Pomodoro timer now automatically saves study time when the app closes unexpectedly, whether due to user closing the app, app crashes, or phone shutdown. **ENHANCED with periodic auto-save every 5 seconds!**

## How It Works

### 1. Timer State Persistence
- **Data Model**: Added `PomodoroTimerState` in `StudyData.kt` to store:
  - Session start time
  - Remaining time
  - Initial duration
  - Selected subject
  - Session type
  - Completed sessions count
  - Playing/paused state

### 2. Repository Changes
Added three new methods to `StudyRepository`:
- `saveTimerState()`: Saves current timer state to DataStore
- `restoreTimerState()`: Retrieves saved timer state
- `clearTimerState()`: Removes saved state after successful save

### 3. ViewModel Auto-Save Logic (ENHANCED)
The `PomodoroViewModel` now:
- **Periodic Save**: Auto-saves every 5 seconds while timer is running ⚡
- **On Timer Start**: Saves initial state immediately
- **On Pause**: Saves timer state automatically
- **On App Background**: Saves timer state via `onAppGoingToBackground()`
- **On ViewModel Destruction**: Saves the session immediately using `onCleared()`
- **On App Restart**: Always saves the session if any time was studied (even if paused)

### 4. Activity Lifecycle Integration
`MainActivity` now:
- Holds a reference to `PomodoroViewModel`
- Calls `onAppGoingToBackground()` in both `onPause()` and `onStop()`
- Ensures auto-save even when app is backgrounded or destroyed

## Auto-Save Scenarios

### Scenario 1: User Force-Closes App from Recent Apps (FIXED!)
1. Timer is running (e.g., 1 minute 15 seconds studied out of 25 minutes)
2. User minimizes app and force-closes it from recent apps menu
3. **Most recent auto-save (within last 5 seconds)** contains the progress
4. On next app launch, `restoreTimerState()` is called
5. Session is automatically saved with ~1 minute 15 seconds of study time
6. Timer resets to 25:00 for a fresh session
7. ✅ **No data loss!**

### Scenario 2: Phone Shuts Down or Battery Dies
1. Timer is running for 15 minutes
2. Phone shuts down suddenly
3. Last periodic save (within last 5 seconds) is persisted in DataStore
4. On next app launch, `restoreTimerState()` is called
5. Session is auto-saved with ~15 minutes of study time
6. Timer resets to allow starting a fresh session
7. ✅ **Maximum 5 seconds of study time can be lost**

### Scenario 3: User Pauses and Closes App
1. User pauses the timer
2. `pauseTimer()` calls `saveTimerState()`
3. User closes the app
4. On next launch, timer state is restored in paused state
5. User can resume or complete the session

### Scenario 4: Timer Completes Normally
1. Timer reaches 00:00
2. Session is saved normally
3. Timer state is cleared via `clearTimerState()`

## Key Features

### Smart State Management
- **Periodic Auto-Save**: Timer state is saved every 5 seconds while running ⚡
- **Immediate Initial Save**: State is saved as soon as timer starts
- **Aggressive Recovery**: Always saves session on restart if any time was studied
- **Never Auto-Resume**: Timer resets to fresh state after recovery (never auto-plays)
- **Break Sessions**: Only WORK sessions are auto-saved (breaks are not tracked)
- **Minimum Duration**: Only sessions with at least 1 second of study time are saved

### Data Integrity
- Periodic saves ensure maximum 5 seconds of data loss on force-kill
- Uses `runBlocking` in `onCleared()` to ensure save completes before ViewModel destruction
- Clears timer state after successful save to prevent duplicate entries
- Handles null cases gracefully

### User Experience
- **Seamless recovery** from force-close scenarios
- **Minimal data loss** (maximum 5 seconds)
- Fresh timer start after recovery (no confusing paused states)
- Total study time display updates correctly with restored sessions

## Files Modified

1. **StudyData.kt**: Added `PomodoroTimerState` data class
2. **StudyRepository.kt**: Added save/restore/clear timer state methods
3. **PomodoroViewModel.kt**: Added auto-save logic and state restoration
4. **MainActivity.kt**: Added lifecycle callbacks for auto-save
5. **PomodoroScreen.kt**: Added ViewModel reference passing

## Testing Recommendations

1. **Test Normal Flow**: Start timer, let it run, pause, resume, complete
2. **Test Force Close**: Start timer, force close app, reopen (should save progress)
3. **Test Background**: Start timer, press home button, kill app from recents (should save progress)
4. **Test Pause and Close**: Pause timer, close app, reopen (should restore paused state)
5. **Test Multiple Sessions**: Complete multiple sessions to ensure no data corruption
6. **Test Break Sessions**: Verify breaks are not saved as study time

## Technical Notes

- Uses Kotlin Coroutines for async operations
- DataStore Preferences for persistent storage
- Gson for JSON serialization with custom LocalDateTime adapter
- Lifecycle-aware components ensure proper cleanup

