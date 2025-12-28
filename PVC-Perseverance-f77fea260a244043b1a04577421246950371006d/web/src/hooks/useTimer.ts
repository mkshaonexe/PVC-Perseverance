import { useEffect, useRef, useCallback } from 'react';
import { useTimerStore } from '../store/useTimerStore';
import { SoundManager } from '../lib/sound';

export function useTimer() {
    const {
        timeLeft,
        initialTime,
        isRunning,
        mode,
        selectedSubject,
        totalStudyTime,
        completedSessions,
        isTimerCompleted,
        isWaitingForAcknowledgment,
        setTimeLeft,
        setIsRunning,
        setSelectedSubject,
        setInitialTime,
        completeSession,
        acknowledgeTimerCompletion: storeAcknowledge,
        startBreakTimer: storeStartBreak,
        startWorkTimer: storeStartWork,
        addToTotalTime
    } = useTimerStore();

    const workerRef = useRef<Worker | null>(null);
    const lastTimeRef = useRef<number>(timeLeft); // Track last time to calculate study seconds
    const isRunningRef = useRef<boolean>(isRunning);
    const modeRef = useRef<string>(mode);
    const subjectRef = useRef<string>(selectedSubject);

    // Keep refs in sync
    isRunningRef.current = isRunning;
    modeRef.current = mode;
    subjectRef.current = selectedSubject;

    // Initialize worker ONCE
    useEffect(() => {
        workerRef.current = new Worker(new URL('../workers/timer.worker.ts', import.meta.url));

        workerRef.current.onmessage = (e) => {
            const { type, payload } = e.data;
            if (type === 'TICK') {
                const previousTime = lastTimeRef.current;
                const currentTime = payload;

                setTimeLeft(currentTime);
                lastTimeRef.current = currentTime;

                // Only add to study time if timer is actually running AND it's a work session
                if (isRunningRef.current && modeRef.current === 'pomodoro' && subjectRef.current !== 'Break') {
                    const elapsed = previousTime - currentTime;
                    if (elapsed > 0 && elapsed <= 2) { // Sanity check: only count reasonable intervals
                        addToTotalTime(elapsed);
                    }
                }
            } else if (type === 'COMPLETE') {
                setIsRunning(false);
                setTimeLeft(0);
                useTimerStore.setState({ isTimerCompleted: true, isWaitingForAcknowledgment: true });
                SoundManager.playAlarm();
            }
        };

        return () => {
            workerRef.current?.terminate();
        };
    }, []); // Empty dependency - worker is created once

    const start = useCallback(() => {
        if (!isRunning) {
            lastTimeRef.current = timeLeft; // Reset tracking
            if (timeLeft === initialTime) {
                workerRef.current?.postMessage({
                    type: 'START',
                    payload: { initialTime: timeLeft }
                });
            } else {
                workerRef.current?.postMessage({
                    type: 'RESUME',
                    payload: { initialTime: timeLeft }
                });
            }
            setIsRunning(true);
        }
    }, [isRunning, timeLeft, initialTime, setIsRunning]);

    const pause = useCallback(() => {
        if (isRunning) {
            workerRef.current?.postMessage({ type: 'PAUSE' });
            setIsRunning(false);
        }
    }, [isRunning, setIsRunning]);

    const reset = useCallback(() => {
        workerRef.current?.postMessage({ type: 'RESET', payload: { initialTime } });
        completeSession();
    }, [initialTime, completeSession]);

    const setDuration = useCallback((minutes: number) => {
        const seconds = minutes * 60;
        setInitialTime(seconds);
        setTimeLeft(seconds);
        lastTimeRef.current = seconds;
        // Also tell worker to reset if not running
        if (!isRunning) {
            workerRef.current?.postMessage({ type: 'RESET', payload: { initialTime: seconds } });
        }
    }, [setInitialTime, setTimeLeft, isRunning]);

    // Wrapper for startBreakTimer that also triggers the worker
    const startBreakTimer = useCallback(() => {
        const breakDuration = useTimerStore.getState().breakDuration;
        storeStartBreak(); // Sets mode, timeLeft, initialTime, isRunning in store
        // Need to also START the worker
        workerRef.current?.postMessage({
            type: 'START',
            payload: { initialTime: breakDuration }
        });
        lastTimeRef.current = breakDuration;
    }, [storeStartBreak]);

    // Wrapper for startWorkTimer
    const startWorkTimer = useCallback((duration?: number) => {
        storeStartWork(duration);
        const time = duration ? duration * 60 : 50 * 60;
        workerRef.current?.postMessage({
            type: 'START',
            payload: { initialTime: time }
        });
        lastTimeRef.current = time;
    }, [storeStartWork]);

    return {
        timeLeft,
        initialTime,
        isRunning,
        mode,
        selectedSubject,
        totalStudyTime,
        completedSessions,
        isTimerCompleted,
        isWaitingForAcknowledgment,
        start,
        pause,
        reset,
        setDuration,
        acknowledgeTimerCompletion: storeAcknowledge,
        startBreakTimer,
        startWorkTimer,
        setSelectedSubject,
        formatTime: (seconds: number) => {
            const m = Math.floor(seconds / 60);
            const s = seconds % 60;
            return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
        },
        formatHours: (seconds: number) => {
            const h = Math.floor(seconds / 3600);
            const m = Math.floor((seconds % 3600) / 60);
            const s = seconds % 60;
            return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
        }
    };
}
