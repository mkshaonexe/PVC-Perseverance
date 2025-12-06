export default function DashboardPage() {
    return (
        <div className="min-h-screen bg-black text-white p-6 pb-24 font-sans">
            <header className="mb-8 mt-4">
                <h1 className="text-2xl font-bold">Dashboard</h1>
            </header>

            {/* Daily Study Time Card - Android Style */}
            <div className="bg-card rounded-2xl p-6 border border-white/5 mb-6">
                <div className="flex items-center gap-4 mb-4">
                    <div className="w-10 h-10 rounded-full bg-secondary flex items-center justify-center">
                        {/* Icon placeholder */}
                        <span className="text-xl">📚</span>
                    </div>
                    <div className="flex-1">
                        <h2 className="text-sm text-white/50">Today&apos;s Total Study Time</h2>
                        <div className="text-2xl font-bold text-secondary tracking-widest">00:00:00</div>
                    </div>
                    <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center">
                        →
                    </div>
                </div>
            </div>

            {/* Placeholder for Chart */}
            <div className="h-64 rounded-2xl bg-[#151515] flex items-center justify-center border border-white/5">
                <span className="text-white/20">Daily Study Time Chart</span>
            </div>
        </div>
    );
}
