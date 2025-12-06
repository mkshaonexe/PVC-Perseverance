import { create } from 'zustand';

export type TimerMode = 'pomodoro' | 'shortBreak' | 'longBreak';

interface TimerState {
    timeLeft: number;
    initialTime: number;
    isRunning: boolean;
    mode: TimerMode;
    selectedSubject: string;
    totalStudyTime: number; // in seconds

    // Actions
    setTimeLeft: (time: number) => void;
    setInitialTime: (time: number) => void;
    setIsRunning: (running: boolean) => void;
    setMode: (mode: TimerMode) => void;
    setSelectedSubject: (subject: string) => void;
    addToTotalTime: (seconds: number) => void;
    resetTimer: () => void;
}

export const useTimerStore = create<TimerState>((set, get) => ({
    timeLeft: 50 * 60, // Default 50 min per Android
    initialTime: 50 * 60,
    isRunning: false,
    mode: 'pomodoro',
    selectedSubject: 'English',
    totalStudyTime: 0,

    setTimeLeft: (time) => set({ timeLeft: time }),
    setInitialTime: (time) => set({ initialTime: time }),
    setIsRunning: (running) => set({ isRunning: running }),
    setMode: (mode) => set({ mode }),
    setSelectedSubject: (subject) => set({ selectedSubject: subject }),
    addToTotalTime: (seconds) => set((state) => ({ totalStudyTime: state.totalStudyTime + seconds })),

    resetTimer: () => {
        const { initialTime } = get();
        set({ timeLeft: initialTime, isRunning: false });
    }
}));
