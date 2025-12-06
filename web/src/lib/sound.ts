export class SoundManager {
    private static audioCtx: AudioContext | null = null;
    private static oscillator: OscillatorNode | null = null;
    private static alarmTimeouts: number[] = [];

    static init() {
        if (!this.audioCtx) {
            this.audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
        }
    }

    static playBeep() {
        this.init();
        if (!this.audioCtx) return;

        // Create oscillator
        const oscillator = this.audioCtx.createOscillator();
        const gainNode = this.audioCtx.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(this.audioCtx.destination);

        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(880, this.audioCtx.currentTime); // A5

        // Envelope
        gainNode.gain.setValueAtTime(0.1, this.audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.00001, this.audioCtx.currentTime + 0.5);

        oscillator.start();
        oscillator.stop(this.audioCtx.currentTime + 0.5);
    }

    static playAlarm() {
        // Clear any existing alarm timeouts
        this.stopAlarm();

        // Play 3 beeps
        this.playBeep();
        this.alarmTimeouts.push(window.setTimeout(() => this.playBeep(), 600));
        this.alarmTimeouts.push(window.setTimeout(() => this.playBeep(), 1200));
    }

    static stopAlarm() {
        // Clear all pending alarm timeouts
        this.alarmTimeouts.forEach(timeoutId => clearTimeout(timeoutId));
        this.alarmTimeouts = [];
    }
}
