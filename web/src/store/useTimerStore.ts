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
        const { mode, completedSessions } = get();
        set({ isTimerCompleted: false, isWaitingForAcknowledgment: false });

        if (mode === 'pomodoro') {
            set({
                completedSessions: completedSessions + 1,
                timeLeft: 50 * 60,
                initialTime: 50 * 60
            });
        } else {
            set({
                mode: 'pomodoro',
                timeLeft: 50 * 60,
                initialTime: 50 * 60
            });
        }
    },

    startBreakTimer: () => {
        set({
            mode: 'shortBreak',
            timeLeft: 10 * 60,
            initialTime: 10 * 60,
            isRunning: true
        });
    },

    startWorkTimer: (duration) => {
        const time = duration ? duration * 60 : 50 * 60;
        set({
            mode: 'pomodoro',
            timeLeft: time,
            initialTime: time,
            isRunning: true
        });
    }
}));
