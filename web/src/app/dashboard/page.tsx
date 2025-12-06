"use client";

import { useTimer } from "@/hooks/useTimer";
import { Play, Pause, Square, ArrowRight } from "lucide-react";
import { cn } from "@/lib/utils";
import { useEffect, useState } from "react";

// Simple analog clock component
function AnalogClock() {
    const [time, setTime] = useState(new Date());

    useEffect(() => {
        const timer = setInterval(() => setTime(new Date()), 1000);
        return () => clearInterval(timer);
    }, []);

    const hours = time.getHours() % 12;
    const minutes = time.getMinutes();
    const seconds = time.getSeconds();

    const hourAngle = (hours * 30) + (minutes * 0.5) - 90;
    const minuteAngle = (minutes * 6) - 90;
    const secondAngle = (seconds * 6) - 90;

    return (
        <div className="relative w-24 h-24">
            {/* Clock face */}
            <svg viewBox="0 0 100 100" className="w-full h-full">
                {/* Outer circle */}
                <circle cx="50" cy="50" r="48" fill="none" stroke="#3C3C3C" strokeWidth="2" />

                {/* Hour markers */}
                {[...Array(12)].map((_, i) => {
                    const angle = (i * 30 - 90) * (Math.PI / 180);
                    const x1 = 50 + 38 * Math.cos(angle);
                    const y1 = 50 + 38 * Math.sin(angle);
                    const x2 = 50 + 44 * Math.cos(angle);
                    const y2 = 50 + 44 * Math.sin(angle);
                    return (
                        <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#FFD700" strokeWidth="2" />
                    );
                })}

                {/* Hour hand */}
                <line
                    x1="50"
                    y1="50"
                    x2={50 + 22 * Math.cos(hourAngle * Math.PI / 180)}
                    y2={50 + 22 * Math.sin(hourAngle * Math.PI / 180)}
                    stroke="#FFFFFF"
                    strokeWidth="3"
                    strokeLinecap="round"
                />

                {/* Minute hand */}
                <line
                    x1="50"
                    y1="50"
                    x2={50 + 32 * Math.cos(minuteAngle * Math.PI / 180)}
                    y2={50 + 32 * Math.sin(minuteAngle * Math.PI / 180)}
                    stroke="#FFFFFF"
                    strokeWidth="2"
                    strokeLinecap="round"
                />

                {/* Second hand */}
                <line
                    x1="50"
                    y1="50"
                    x2={50 + 36 * Math.cos(secondAngle * Math.PI / 180)}
                    y2={50 + 36 * Math.sin(secondAngle * Math.PI / 180)}
                    stroke="#FFD700"
                    strokeWidth="1"
                    strokeLinecap="round"
                />

                {/* Center dot */}
                <circle cx="50" cy="50" r="3" fill="#FFD700" />
            </svg>
        </div>
    );
}

// Bar Chart Component
function StudyTimeBarChart({ subjects, heightClass = "h-48" }: { subjects: { name: string; minutes: number; color: string }[], heightClass?: string }) {
    const maxMinutes = Math.max(...subjects.map(s => s.minutes), 1);

    return (
        <div className={`w-full ${heightClass} flex items-end justify-around gap-2 px-4`}>
            {subjects.map((subject, index) => {
                const heightPercent = (subject.minutes / maxMinutes) * 100;
                return (
                    <div key={index} className="flex flex-col items-center flex-1">
                        <span className="text-xs text-white mb-1 font-bold">{subject.minutes}m</span>
                        <div
                            className="w-full rounded-t-sm transition-all duration-300"
                            style={{
                                height: `${Math.max(heightPercent, 5)}%`,
                                backgroundColor: subject.color,
                                minHeight: '8px'
                            }}
                        />
                        <span className="text-[10px] text-white/60 mt-2 truncate w-full text-center">{subject.name}</span>
                    </div>
                );
            })}
            {subjects.length === 0 && (
                <div className="text-white/50 text-center w-full py-8">No study data available</div>
            )}
        </div>
    );
}

export default function DashboardPage() {
    const {
        timeLeft,
        isRunning,
        totalStudyTime,
        start,
        pause,
        reset,
        formatTime,
        formatHours
    } = useTimer();

    // Mock subject data for chart (in a real app, this comes from stored sessions)
    const [subjects] = useState([
        { name: 'English', minutes: 45, color: '#FF6B6B' },
        { name: 'Math', minutes: 30, color: '#4ECDC4' },
        { name: 'Science', minutes: 20, color: '#45B7D1' },
        { name: 'Coding', minutes: 60, color: '#96CEB4' },
    ]);

    return (
        <div className="min-h-screen bg-black text-white pb-24">
            <div className="max-w-6xl mx-auto w-full">
                {/* Header */}
                <div className="flex justify-between items-center p-6">
                    <button className="p-2">
                        <div className="space-y-1.5">
                            <div className="w-6 h-0.5 bg-white"></div>
                            <div className="w-6 h-0.5 bg-white"></div>
                            <div className="w-6 h-0.5 bg-white"></div>
                        </div>
                    </button>
                    {/* Add Settings or Profile button here if needed for symmetry/utility */}
                </div>

                <div className="px-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Timer Card with Analog Clock */}
                    <div className="bg-[#1E1E1E] rounded-2xl p-6 border border-white/5 col-span-1 lg:col-span-2">
                        <div className="flex flex-col sm:flex-row items-center justify-between gap-6 sm:gap-0">
                            {/* Analog Clock */}
                            <AnalogClock />

                            {/* Remaining Time */}
                            <div className="flex flex-col items-center">
                                <span className="text-white/50 text-sm mb-1">Remaining time</span>
                                <span className="text-5xl font-bold tabular-nums tracking-tight">{formatTime(timeLeft)}</span>

                                {/* Control Buttons */}
                                <div className="flex gap-3 mt-4">
                                    {isRunning ? (
                                        <button
                                            onClick={pause}
                                            className="w-12 h-12 bg-[#2C2C2C] rounded-xl flex items-center justify-center hover:bg-[#3C3C3C] transition-all hover:scale-105 active:scale-95"
                                        >
                                            <Pause className="w-6 h-6 text-primary" />
                                        </button>
                                    ) : (
                                        <button
                                            onClick={start}
                                            className="w-12 h-12 bg-[#2C2C2C] rounded-xl flex items-center justify-center hover:bg-[#3C3C3C] transition-all hover:scale-105 active:scale-95"
                                        >
                                            <Play className="w-6 h-6 text-primary fill-primary" />
                                        </button>
                                    )}
                                    <button
                                        onClick={reset}
                                        className="w-12 h-12 bg-[#2C2C2C] rounded-xl flex items-center justify-center hover:bg-[#3C3C3C] transition-all hover:scale-105 active:scale-95"
                                    >
                                        <Square className="w-5 h-5 text-primary fill-primary" />
                                    </button>
                                </div>
                            </div>

                            {/* Today's Time Mini Display */}
                            <div className="flex flex-col items-center pt-2 sm:pt-0 border-t sm:border-t-0 border-white/5 w-full sm:w-auto mt-2 sm:mt-0">
                                <div className="w-16 h-16 bg-[#2C2C2C] rounded-2xl flex items-center justify-center mb-2 shadow-inner">
                                    <span className="text-3xl">📚</span>
                                </div>
                                <span className="text-primary text-sm font-medium tabular-nums">{formatHours(totalStudyTime)}</span>
                            </div>
                        </div>
                    </div>

                    {/* Today's Total Study Time - Green Card */}
                    <div className="bg-[#4CAF50]/10 rounded-2xl p-6 border border-[#4CAF50]/20 col-span-1 lg:col-span-1 flex flex-col justify-center">
                        <div className="flex items-center justify-between gap-4">
                            {/* Icon */}
                            <div className="w-14 h-14 bg-[#4CAF50] rounded-full flex items-center justify-center shadow-lg shadow-[#4CAF50]/20 shrink-0">
                                <span className="text-2xl">🌱</span>
                            </div>

                            {/* Text */}
                            <div className="flex flex-col items-start flex-1 min-w-0">
                                <span className="text-white/60 text-xs font-medium uppercase tracking-wider mb-1">Today's Total</span>
                                <span className="text-3xl font-bold text-[#4CAF50] tracking-tight tabular-nums truncate w-full">
                                    {formatHours(totalStudyTime)}
                                </span>
                            </div>

                            {/* Arrow */}
                            <div className="w-10 h-10 bg-[#4CAF50]/20 rounded-full flex items-center justify-center hover:bg-[#4CAF50]/30 transition-colors cursor-pointer shrink-0">
                                <ArrowRight className="w-5 h-5 text-[#4CAF50]" />
                            </div>
                        </div>
                    </div>

                    {/* Daily Study Time Chart */}
                    <div className="bg-[#1E1E1E] rounded-2xl p-6 border border-white/5 col-span-1 lg:col-span-3 min-h-[55vh] flex flex-col justify-between">
                        <div className="flex items-center justify-between mb-8">
                            <h2 className="text-lg font-bold text-white flex items-center gap-2">
                                <span className="w-1 h-6 bg-primary rounded-full"></span>
                                Daily Study Time
                            </h2>
                            <button className="text-xs text-white/40 hover:text-white transition-colors bg-[#2C2C2C] px-3 py-1.5 rounded-full font-medium">
                                Last 7 Days
                            </button>
                        </div>

                        <div className="flex-1 flex flex-col justify-center">
                            <StudyTimeBarChart subjects={subjects} heightClass="h-64" />
                        </div>

                        {/* Legend */}
                        <div className="mt-8 flex flex-wrap gap-4 justify-center">
                            {subjects.map((subject, index) => (
                                <div key={index} className="flex items-center gap-2 bg-[#2C2C2C] px-3 py-1.5 rounded-lg border border-white/5">
                                    <div
                                        className="w-2.5 h-2.5 rounded-full"
                                        style={{ backgroundColor: subject.color }}
                                    />
                                    <span className="text-white/90 text-sm font-medium">{subject.name}</span>
                                    <span className="text-white/40 text-xs ml-1 border-l border-white/10 pl-2">{subject.minutes}m</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
