"use client";

import Image from "next/image";
import { useTimer } from "@/hooks/useTimer";
import { Play, Pause, Check, Plus, Coffee } from "lucide-react";
import { cn } from "@/lib/utils";
import { useEffect, useState } from "react";
import { SoundManager } from "@/lib/sound";
import { motion, AnimatePresence } from "framer-motion";

export default function Home() {
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
    start,
    pause,
    reset,
    setDuration,
    acknowledgeTimerCompletion,
    startBreakTimer,
    setSelectedSubject,
    formatTime,
    formatHours
  } = useTimer();

  const [showSubjectDialog, setShowSubjectDialog] = useState(false);
  const [showDurationDialog, setShowDurationDialog] = useState(false);

  // Initialize Audio
  useEffect(() => {
    const primeAudio = () => SoundManager.init();
    window.addEventListener('click', primeAudio);
    return () => window.removeEventListener('click', primeAudio);
  }, []);

  // Determine current illustration
  const illustrationSrc = isRunning ? "/study.png" : "/home.png";

  return (
    <div className="flex flex-col h-screen bg-black text-white overflow-hidden pb-safe">

      {/* Top Bar */}
      <div className="flex justify-between items-center p-6 mt-safe">
        <button className="p-2">
          <div className="space-y-1.5">
            <div className="w-6 h-0.5 bg-white"></div>
            <div className="w-6 h-0.5 bg-white"></div>
            <div className="w-6 h-0.5 bg-white"></div>
          </div>
        </button>
        <div className="flex gap-4">
          {/* Icons would go here */}
        </div>
      </div>

      {/* Main Content */}
      <main className="flex-1 flex flex-col items-center justify-center -mt-10">

        {/* Subject */}
        {mode === 'pomodoro' ? (
          <div className="flex items-center gap-2 mb-2 p-2 rounded-full active:bg-white/10 transition-colors cursor-pointer"
            onClick={() => !isRunning && setShowSubjectDialog(true)}>
            <div className="w-2 h-2 rounded-full bg-primary"></div>
            <span className="text-muted-foreground text-sm uppercase tracking-wide">
              {selectedSubject}
            </span>
          </div>
        ) : (
          <div className="flex items-center gap-2 mb-2 p-2">
            <div className="w-2 h-2 rounded-full bg-[#2196F3]"></div>
            <span className="text-[#2196F3] text-sm uppercase tracking-wide">
              Break
            </span>
          </div>
        )}

        {/* Digital Clock (Clickable) */}
        <div
          className="text-[5rem] font-light tracking-tighter tabular-nums leading-none mb-8 select-none cursor-pointer active:opacity-70 transition-opacity"
          onClick={() => !isRunning && setShowDurationDialog(true)}
        >
          {formatTime(timeLeft)}
        </div>

        {/* Pagination Dots */}
        <div className="flex gap-2 mb-12">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className={cn("w-2 h-2 rounded-full", i <= completedSessions ? "bg-[#4CAF50]" : "bg-white/30")}></div>
          ))}
        </div>

        {/* Illustration */}
        <div className="relative w-48 h-48 mb-12 opacity-80 filter grayscale-[0.2]">
          <Image
            src={illustrationSrc}
            alt="Illustration"
            fill
            className="object-contain"
            priority
          />
        </div>

        {/* Total Study Time Label */}
        <div className="text-white/50 text-sm mb-2">
          Today&apos;s Total Study Time
        </div>

        {/* Total Study Time Value (Green) */}
        <div className="text-3xl font-bold text-secondary tracking-widest mb-12 tabular-nums">
          {formatHours(totalStudyTime)}
        </div>

        {/* Action Buttons */}
        <div className="w-full max-w-md px-12 mx-auto">
          {isWaitingForAcknowledgment ? (
            // "I got it" Button (Green)
            <button
              onClick={acknowledgeTimerCompletion}
              className="w-full h-14 bg-secondary text-white font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-all shadow-[0_0_20px_rgba(76,175,80,0.4)]">
              <Check className="w-5 h-5" />
              I got it
            </button>
          ) : !isRunning ? (
            // Idle State
            completedSessions > 0 && mode === 'pomodoro' ? (
              // Show "Take a Break" AND "Start Focus"
              <div className="flex flex-col gap-4">
                <button
                  onClick={() => {
                    SoundManager.stopAlarm();
                    startBreakTimer();
                  }}
                  className="w-full h-14 bg-[#2196F3] text-white font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-all">
                  <Coffee className="w-5 h-5" />
                  Take a Break
                </button>
                <button
                  onClick={start}
                  className="w-full h-14 bg-primary text-black font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-all">
                  <Play className="fill-black w-5 h-5" />
                  Start Focus
                </button>
              </div>
            ) : (
              // Default Start
              <button
                onClick={start}
                className="w-full h-14 bg-primary text-black font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-all shadow-[0_0_20px_rgba(255,215,0,0.2)]">
                <Play className="fill-black w-5 h-5" />
                Start Focus
              </button>
            )
          ) : (
            // Running/Paused State
            <div className="flex gap-4">
              <button
                onClick={pause}
                className="flex-1 h-14 bg-primary text-black font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-transform">
                <Pause className="fill-black w-5 h-5" />
                Pause
              </button>
              <button
                onClick={reset} // "Done"
                className="flex-1 h-14 bg-secondary text-white font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-transform">
                <Check className="w-5 h-5" />
                Done
              </button>
            </div>
          )}
        </div>

      </main>

      {/* Dialogs */}
      <AnimatePresence>
        {/* Subject Dialog */}
        {showSubjectDialog && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-8">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="bg-[#2C2C2C] w-full max-w-sm rounded-[2rem] overflow-hidden border border-white/5"
            >
              <div className="p-6">
                <h2 className="text-xl font-bold mb-6 text-white">Select Subject</h2>
                <div className="space-y-4 max-h-[60vh] overflow-y-auto">
                  {['English', 'Math', 'Science', 'Coding', 'Break'].map(sub => (
                    <button
                      key={sub}
                      onClick={() => { setSelectedSubject(sub); setShowSubjectDialog(false); }}
                      className={cn(
                        "w-full p-4 rounded-xl flex justify-between items-center text-left transition-colors",
                        selectedSubject === sub ? "bg-[#3C3220]" : "bg-[#3C3C3C]"
                      )}
                    >
                      <span className={cn(selectedSubject === sub ? "text-white font-medium" : "text-white ml-2")}>{sub}</span>
                      {selectedSubject === sub && <Check className="w-5 h-5 text-primary" />}
                    </button>
                  ))}
                  <button className="w-full p-4 rounded-xl flex items-center justify-center gap-2 text-primary mt-2">
                    <Plus className="w-5 h-5" />
                    Add New Subject
                  </button>
                </div>
              </div>
            </motion.div>
            <div className="absolute inset-0 -z-10" onClick={() => setShowSubjectDialog(false)} />
          </div>
        )}

        {/* Duration Dialog */}
        {showDurationDialog && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-8">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="bg-[#2C2C2C] w-full max-w-sm rounded-[2rem] overflow-hidden border border-white/5"
            >
              <div className="p-6">
                <h2 className="text-xl font-bold mb-6 text-white">Change Timer Duration</h2>
                <div className="space-y-2">
                  {[5, 10, 25, 50, 60].map(mins => (
                    <button
                      key={mins}
                      onClick={() => { setDuration(mins); setShowDurationDialog(false); }}
                      className="w-full p-4 rounded-xl bg-[#3C3C3C] text-white text-left hover:bg-[#4a4a4a] transition-colors font-medium">
                      {mins} minutes
                    </button>
                  ))}
                  <button className="w-full p-4 rounded-xl bg-primary text-black font-medium mt-2">
                    Custom
                  </button>
                  <button
                    onClick={() => setShowDurationDialog(false)}
                    className="w-full p-2 text-center text-white/50 mt-4 text-sm">
                    Cancel
                  </button>
                </div>
              </div>
            </motion.div>
            <div className="absolute inset-0 -z-10" onClick={() => setShowDurationDialog(false)} />
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
