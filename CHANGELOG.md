# Changelog

## Version 0.2.5 - October 19, 2025, 2:25 PM

### New Features
- **Break Timer Sound**: Added 1000 Hz sine wave beep sound when break timer completes
- **Customizable Break Duration**: Users can now set break duration in Settings (5, 10, 15, 20 minutes or custom)
- **Dynamic Button Text**: "Take a Break" button changes to "Start Focus" after break completion
- **Break Status Indicator**: Shows "Break" text with green dot when break timer is running

### Improvements
- **Maximum Sound Alerts**: Increased beep volume to 100% for maximum attention
- **Enhanced UX**: Break sessions now have the same acknowledgment flow as work sessions
- **Visual Feedback**: Clear indication of current session type (work vs break)

### Technical Changes
- Added `breakDuration` setting to SettingsRepository
- Updated PomodoroViewModel to use customizable break duration
- Enhanced TimerSoundService with louder sine wave generation
- Improved UI state management for break sessions

---

## Version 0.1.9-beta - Previous Release
- Initial Pomodoro timer implementation
- Basic work session tracking
- Settings page functionality
- Video background support
