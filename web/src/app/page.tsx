"use client";

import Image from "next/image";
import { useTimer } from "@/hooks/useTimer";
import { Play, Pause, Check, Plus, X } from "lucide-react";
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
    start,
    pause,
    reset,
    setSelectedSubject,
    formatTime,
    formatHours
  } = useTimer();

  const [showSubjectDialog, setShowSubjectDialog] = useState(false);

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

        {/* Subject & Language */}
        <div className="flex items-center gap-2 mb-2 p-2 rounded-full active:bg-white/10 transition-colors cursor-pointer"
          onClick={() => !isRunning && setShowSubjectDialog(true)}>
          <div className="w-2 h-2 rounded-full bg-primary"></div>
          <span className="text-muted-foreground text-sm uppercase tracking-wide">
            {selectedSubject}
          </span>
        </div>

        {/* Digital Clock */}
        <div className="text-[5rem] font-light tracking-tighter tabular-nums leading-none mb-8 select-none">
          {formatTime(timeLeft)}
        </div>

        {/* Pagination Dots (Visual Only) */}
        <div className="flex gap-2 mb-12">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className={cn("w-2 h-2 rounded-full", i === 1 ? "bg-white" : "bg-white/30")}></div>
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

        {/* Action Button */}
        <div className="w-full px-12">
          {!isRunning ? (
            <button
              onClick={start}
              className="w-full h-14 bg-primary text-black font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-all shadow-[0_0_20px_rgba(255,215,0,0.2)]">
              <Play className="fill-black w-5 h-5" />
              Start Focus
            </button>
          ) : (
            <div className="flex gap-4">
              <button
                onClick={pause}
                className="flex-1 h-14 bg-primary text-black font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-transform">
                <Pause className="fill-black w-5 h-5" />
                Pause
              </button>
              <button
                onClick={() => { pause(); reset(); }} // 'Done' usually resets/stops logic
                className="flex-1 h-14 bg-secondary text-white font-bold text-lg rounded-full flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-95 transition-transform">
                <Check className="w-5 h-5" />
                Done
              </button>
            </div>
          )}
        </div>

      </main>

      {/* Subject Selection Dialog Overlay */}
      <AnimatePresence>
        {showSubjectDialog && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-8">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="bg-card w-full max-w-sm rounded-[2rem] overflow-hidden border border-white/10"
            >
              <div className="p-6">
                <h2 className="text-xl font-bold mb-6">Select Subject</h2>

                <div className="space-y-2">
                  {['English', 'Math', 'Science', 'Coding'].map(sub => (
                    <button
                      key={sub}
                      onClick={() => { setSelectedSubject(sub); setShowSubjectDialog(false); }}
                      className={cn(
                        "w-full p-4 rounded-xl flex justify-between items-center text-left transition-colors",
                        selectedSubject === sub ? "bg-[#3C3220] text-primary" : "bg-white/5 hover:bg-white/10"
                      )}
                    >
                      <span>{sub}</span>
                      {selectedSubject === sub && <Check className="w-5 h-5" />}
                    </button>
                  ))}

                  <button className="w-full p-4 rounded-xl flex items-center justify-center gap-2 text-primary mt-4 hover:bg-white/5">
                    <Plus className="w-5 h-5" />
                    Add New Subject
                  </button>
                </div>
              </div>
            </motion.div>

            {/* Close Backdrop Click */}
            <div className="absolute inset-0 -z-10" onClick={() => setShowSubjectDialog(false)} />
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
