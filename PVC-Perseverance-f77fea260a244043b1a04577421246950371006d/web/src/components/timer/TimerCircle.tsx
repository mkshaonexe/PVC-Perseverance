import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";

interface TimerCircleProps {
    timeLeft: number;
    initialTime: number;
    label?: string;
    className?: string;
}

export function TimerCircle({ timeLeft, initialTime, label = "FOCUS", className }: TimerCircleProps) {
    // Calculate percentage
    const percentage = (timeLeft / initialTime) * 100;
    const radius = 120;
    const circumference = 2 * Math.PI * radius;
    const strokeDashoffset = circumference - (percentage / 100) * circumference;

    // Format time
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    const timeString = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

    return (
        <div className={cn("relative flex items-center justify-center", className)}>
            {/* SVG Container */}
            <div className="relative h-80 w-80">
                {/* SVG Definition for Gradient */}
                <svg className="absolute h-full w-full rotate-[-90deg]">
                    <defs>
                        <linearGradient id="goldGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                            <stop offset="0%" stopColor="#d4af37" />
                            <stop offset="100%" stopColor="#f7ef8a" />
                        </linearGradient>
                        <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                            <feGaussianBlur stdDeviation="4" result="coloredBlur" />
                            <feMerge>
                                <feMergeNode in="coloredBlur" />
                                <feMergeNode in="SourceGraphic" />
                            </feMerge>
                        </filter>
                    </defs>

                    {/* Background Track */}
                    <circle
                        cx="50%"
                        cy="50%"
                        r={radius}
                        stroke="currentColor"
                        strokeWidth="8"
                        fill="transparent"
                        className="text-secondary"
                    />

                    {/* Progress Indicator */}
                    <motion.circle
                        cx="50%"
                        cy="50%"
                        r={radius}
                        stroke="url(#goldGradient)"
                        strokeWidth="10"
                        fill="transparent"
                        strokeLinecap="round"
                        style={{ strokeDasharray: circumference }}
                        animate={{ strokeDashoffset }}
                        transition={{ duration: 1, ease: "linear" }}
                        filter="url(#glow)"
                    />
                </svg>

                {/* Text Content */}
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                    <motion.div
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        key={label}
                        className="text-primary/80 tracking-[0.2em] text-sm font-medium mb-2 uppercase"
                    >
                        {label}
                    </motion.div>

                    <motion.div
                        key={timeString}
                        className="text-6xl font-black tracking-tighter text-foreground tabular-nums select-none"
                        initial={{ scale: 0.95, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ type: "spring", stiffness: 300, damping: 30 }}
                    >
                        {timeString}
                    </motion.div>
                </div>
            </div>
        </div>
    );
}
