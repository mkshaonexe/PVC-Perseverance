import { create } from 'zustand';

export type TimerMode = 'pomodoro' | 'shortBreak' | 'longBreak';

interface TimerState {
    timeLeft: number;
    initialTime: number;
    isRunning: boolean;
    mode: TimerMode;
    selectedSubject: string;
    totalStudyTime: number; // in seconds
    completedSessions: number;
    isTimerCompleted: boolean;
    isWaitingForAcknowledgment: boolean;
    breakDuration: number; // in seconds, default 10 minutes
    previousWorkSubject: string; // Track subject before break

    // Actions
    setTimeLeft: (time: number) => void;
    setInitialTime: (time: number) => void;
    setIsRunning: (running: boolean) => void;
    setMode: (mode: TimerMode) => void;
    setSelectedSubject: (subject: string) => void;
    addToTotalTime: (seconds: number) => void;

    // Complex Logic
    resetTimer: () => void;
    completeSession: () => void;
    acknowledgeTimerCompletion: () => void;
    startBreakTimer: () => void;
    startWorkTimer: (duration?: number) => void;
}

export const useTimerStore = create<TimerState>((set, get) => ({
    timeLeft: 50 * 60, // Default 50 min per Android
    initialTime: 50 * 60,
    isRunning: false,
    mode: 'pomodoro',
    selectedSubject: 'English',
    totalStudyTime: 0,
    completedSessions: 0,
    isTimerCompleted: false,
    isWaitingForAcknowledgment: false,
    breakDuration: 10 * 60, // Default 10 minutes per Android
    previousWorkSubject: 'English',

    setTimeLeft: (time) => set({ timeLeft: time }),
    setInitialTime: (time) => set({ initialTime: time }),
    setIsRunning: (running) => set({ isRunning: running }),
    setMode: (mode) => set({ mode }),
    setSelectedSubject: (subject) => set({ selectedSubject: subject }),
    addToTotalTime: (seconds) => set((state) => ({ totalStudyTime: state.totalStudyTime + seconds })),

    resetTimer: () => {
        const { initialTime } = get();
        set({ timeLeft: initialTime, isRunning: false, isTimerCompleted: false, isWaitingForAcknowledgment: false });
    },

    completeSession: () => {
        const { initialTime } = get();
        set({ timeLeft: initialTime, isRunning: false, isTimerCompleted: false, isWaitingForAcknowledgment: false });
    },

    acknowledgeTimerCompletion: () => {
        const { mode, completedSessions, previousWorkSubject } = get();
        set({ isTimerCompleted: false, isWaitingForAcknowledgment: false });

        if (mode === 'pomodoro') {
            // Work session completed - increment sessions and reset to work mode
            set({
                completedSessions: completedSessions + 1,
                timeLeft: 50 * 60,
                initialTime: 50 * 60
            });
        } else {
            // Break completed - transition back to work mode and restore previous subject
            set({
                mode: 'pomodoro',
                selectedSubject: previousWorkSubject,
                timeLeft: 50 * 60,
                initialTime: 50 * 60
            });
        }
    },

    startBreakTimer: () => {
        const { breakDuration, completedSessions, selectedSubject, mode } = get();

        // Save current work subject before switching to break
        const workSubject = mode === 'pomodoro' ? selectedSubject : get().previousWorkSubject;

        set({
            mode: 'shortBreak',
            selectedSubject: 'Break', // Automatically set to "Break" subject
            previousWorkSubject: workSubject, // Save for restoration after break
            timeLeft: breakDuration,
            initialTime: breakDuration,
            isRunning: true,
            completedSessions: mode === 'pomodoro' ? completedSessions + 1 : completedSessions, // Increment if coming from work
            isTimerCompleted: false,
            isWaitingForAcknowledgment: false
        });
    },

    startWorkTimer: (duration) => {
        const time = duration ? duration * 60 : 50 * 60;
        const { previousWorkSubject } = get();
        set({
            mode: 'pomodoro',
            selectedSubject: previousWorkSubject, // Restore previous work subject
            timeLeft: time,
            initialTime: time,
            isRunning: true
        });
    }
}));

