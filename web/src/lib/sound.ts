export class SoundManager {
    private static audioCtx: AudioContext | null = null;
    private static oscillator: OscillatorNode | null = null;

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
        // Play 3 beeps
        this.playBeep();
        setTimeout(() => this.playBeep(), 600);
        setTimeout(() => this.playBeep(), 1200);
    }
}
