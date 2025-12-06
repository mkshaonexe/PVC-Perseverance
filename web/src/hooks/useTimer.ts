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
        setTimeLeft,
        setIsRunning,
        resetTimer,
        setSelectedSubject,
        addToTotalTime
    } = useTimerStore();

    const workerRef = useRef<Worker | null>(null);

    useEffect(() => {
        // Initialize Worker
        workerRef.current = new Worker(new URL('../workers/timer.worker.ts', import.meta.url));

        workerRef.current.onmessage = (e) => {
            const { type, payload } = e.data;
            if (type === 'TICK') {
                const diff = timeLeft - payload;
                // If we want to track real-time study stats, we might add diff here?
                // But better to just update displayed time.
                setTimeLeft(payload);
            } else if (type === 'COMPLETE') {
                setIsRunning(false);
                setTimeLeft(0);
                // Add the completed time to total
                addToTotalTime(initialTime);
                SoundManager.playAlarm();
            }
        };

        return () => {
            workerRef.current?.terminate();
        };
    }, [setTimeLeft, setIsRunning, addToTotalTime, initialTime, timeLeft]);

    const start = () => {
        if (!isRunning) {
            // Decide whether to START or RESUME
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

    const reset = () => {
        workerRef.current?.postMessage({ type: 'RESET', payload: { initialTime } });
        resetTimer();
    };

    return {
        timeLeft,
        initialTime,
        isRunning,
        mode,
        selectedSubject,
        totalStudyTime,
        start,
        pause,
        reset,
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
