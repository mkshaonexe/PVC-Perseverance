"use client";

import { LayoutGrid, Home, Users, Settings } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

export function BottomNav() {
    const pathname = usePathname();

    const navItems = [
        { name: "Dashboard", href: "/dashboard", icon: LayoutGrid },
        { name: "Home", href: "/", icon: Home },
        { name: "Groups", href: "/groups", icon: Users },
        { name: "Settings", href: "/settings", icon: Settings },
    ];

    return (
        <div className="fixed bottom-0 left-0 right-0 bg-[#000000] border-t border-white/5 pb-safe z-50">
            <div className="flex justify-around items-center h-16 max-w-md mx-auto">
                {navItems.map((item) => {
                    const isActive = pathname === item.href;
                    return (
                        <Link
                            key={item.name}
                            href={item.href}
                            className={cn(
                                "flex flex-col items-center justify-center w-full h-full space-y-1 transition-colors",
                                isActive ? "text-primary" : "text-white/40"
                            )}
                        >
                            <item.icon className={cn("w-6 h-6", isActive && "fill-current")} />
                        </Link>
                    );
                })}
            </div>
        </div>
    );
}
