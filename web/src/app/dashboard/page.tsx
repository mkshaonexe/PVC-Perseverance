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
function StudyTimeBarChart({ subjects }: { subjects: { name: string; minutes: number; color: string }[] }) {
    const maxMinutes = Math.max(...subjects.map(s => s.minutes), 1);

    return (
        <div className="w-full h-48 flex items-end justify-around gap-2 px-4">
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
            {/* Header */}
            <div className="flex justify-between items-center p-6">
                <button className="p-2">
                    <div className="space-y-1.5">
                        <div className="w-6 h-0.5 bg-white"></div>
                        <div className="w-6 h-0.5 bg-white"></div>
                        <div className="w-6 h-0.5 bg-white"></div>
                    </div>
                </button>
            </div>

            <div className="px-6 space-y-6">
                {/* Timer Card with Analog Clock */}
                <div className="bg-[#1E1E1E] rounded-2xl p-5 border border-white/5">
                    <div className="flex items-center justify-between">
                        {/* Analog Clock */}
                        <AnalogClock />

                        {/* Remaining Time */}
                        <div className="flex flex-col items-center">
                            <span className="text-white/50 text-sm mb-1">Remaining time</span>
                            <span className="text-4xl font-bold tabular-nums">{formatTime(timeLeft)}</span>

                            {/* Control Buttons */}
                            <div className="flex gap-2 mt-3">
                                {isRunning ? (
                                    <button
                                        onClick={pause}
                                        className="w-10 h-10 bg-[#2C2C2C] rounded-lg flex items-center justify-center hover:bg-[#3C3C3C] transition-colors"
                                    >
                                        <Pause className="w-5 h-5 text-primary" />
                                    </button>
                                ) : (
                                    <button
                                        onClick={start}
                                        className="w-10 h-10 bg-[#2C2C2C] rounded-lg flex items-center justify-center hover:bg-[#3C3C3C] transition-colors"
                                    >
                                        <Play className="w-5 h-5 text-primary fill-primary" />
                                    </button>
                                )}
                                <button
                                    onClick={reset}
                                    className="w-10 h-10 bg-[#2C2C2C] rounded-lg flex items-center justify-center hover:bg-[#3C3C3C] transition-colors"
                                >
                                    <Square className="w-4 h-4 text-primary fill-primary" />
                                </button>
                            </div>
                        </div>

                        {/* Today's Time Mini Display */}
                        <div className="flex flex-col items-center">
                            <div className="w-16 h-16 bg-[#2C2C2C] rounded-xl flex items-center justify-center mb-2">
                                <span className="text-3xl">📚</span>
                            </div>
                            <span className="text-primary text-sm font-medium tabular-nums">{formatHours(totalStudyTime)}</span>
                        </div>
                    </div>
                </div>

                {/* Today's Total Study Time - Green Card */}
                <div className="bg-[#4CAF50]/20 rounded-2xl p-5 border border-[#4CAF50]/30">
                    <div className="flex items-center justify-between">
                        {/* Icon */}
                        <div className="w-12 h-12 bg-[#4CAF50] rounded-full flex items-center justify-center">
                            <span className="text-2xl">📚</span>
                        </div>

                        {/* Text */}
                        <div className="flex flex-col items-center flex-1 mx-4">
                            <span className="text-white/70 text-sm">Today&apos;s Total Study Time</span>
                            <span className="text-2xl font-bold text-[#4CAF50] tracking-wider tabular-nums">
                                {formatHours(totalStudyTime)}
                            </span>
                        </div>

                        {/* Arrow */}
                        <div className="w-8 h-8 bg-[#4CAF50] rounded-full flex items-center justify-center">
                            <ArrowRight className="w-4 h-4 text-white" />
                        </div>
                    </div>
                </div>

                {/* Daily Study Time Chart */}
                <div className="bg-[#1E1E1E] rounded-2xl p-5 border border-white/5">
                    <h2 className="text-xl font-bold text-center mb-6">Daily Study Time</h2>

                    <StudyTimeBarChart subjects={subjects} />

                    {/* Legend */}
                    <div className="mt-6 space-y-3">
                        {subjects.map((subject, index) => (
                            <div key={index} className="flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <div
                                        className="w-3 h-3 rounded-sm"
                                        style={{ backgroundColor: subject.color }}
                                    />
                                    <span className="text-white text-sm">{subject.name}</span>
                                </div>
                                <span className="text-white/70 text-sm font-medium">{subject.minutes}m</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
