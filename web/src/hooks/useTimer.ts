import { useEffect, useRef } from 'react';
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
        resetTimer,
        setSelectedSubject,
        addToTotalTime,
        acknowledgeTimerCompletion,
        startBreakTimer,
        startWorkTimer,
        setInitialTime,
        completeSession
    } = useTimerStore();

    const workerRef = useRef<Worker | null>(null);

    useEffect(() => {
        workerRef.current = new Worker(new URL('../workers/timer.worker.ts', import.meta.url));

        workerRef.current.onmessage = (e) => {
            const { type, payload } = e.data;
            if (type === 'TICK') {
                setTimeLeft(payload);
                if (mode === 'pomodoro' && selectedSubject !== 'Break') {
                    addToTotalTime(1);
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
    }, [setTimeLeft, setIsRunning, addToTotalTime, mode, selectedSubject, initialTime]);

    const start = () => {
        if (!isRunning) {
            if (timeLeft === initialTime) {
                workerRef.current?.postMessage({
                    type: 'START',
                    payload: { initialTime: initialTime }
                });
            } else {
                workerRef.current?.postMessage({
                    type: 'RESUME',
                    payload: { initialTime: timeLeft }
                });
            }
            setIsRunning(true);
        }
    };

    const pause = () => {
        if (isRunning) {
            workerRef.current?.postMessage({ type: 'PAUSE' });
            setIsRunning(false);
        }
    };

    const setDuration = (minutes: number) => {
        const seconds = minutes * 60;
        setInitialTime(seconds);
        setTimeLeft(seconds);
    };

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
        reset: completeSession,
        setDuration,
        acknowledgeTimerCompletion,
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
