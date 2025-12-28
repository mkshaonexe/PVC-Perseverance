export default function SettingsPage() {
    return (
        <div className="flex min-h-screen flex-col bg-background p-8 pb-32">
            <h1 className="text-2xl font-bold tracking-tighter text-primary/80 uppercase mb-8">
                Settings
            </h1>

            <div className="space-y-6">
                <div className="space-y-2">
                    <h3 className="text-lg font-medium text-foreground">Timer Settings</h3>
                    <div className="rounded-lg border border-white/10 bg-card p-4">
                        <div className="flex items-center justify-between py-2">
                            <span className="text-muted-foreground">Focus Duration</span>
                            <span className="text-primary">25 min</span>
                        </div>
                        <div className="flex items-center justify-between py-2 border-t border-white/5">
                            <span className="text-muted-foreground">Short Break</span>
                            <span className="text-primary">5 min</span>
                        </div>
                    </div>
                </div>

                <div className="space-y-2">
                    <h3 className="text-lg font-medium text-foreground">Appearance</h3>
                    <div className="rounded-lg border border-white/10 bg-card p-4">
                        <div className="flex items-center justify-between py-2">
                            <span className="text-muted-foreground">Theme</span>
                            <span className="text-primary">Deep Gold</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
