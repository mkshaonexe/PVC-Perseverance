/* eslint-disable no-restricted-globals */
// Web Worker for accurate timing

let timerInterval: NodeJS.Timeout | null = null;
let expectedTime: number = 0;
let remainingTime: number = 0;

self.onmessage = (e: MessageEvent) => {
    const { type, payload } = e.data;

    switch (type) {
        case "START":
            if (timerInterval) return; // Already running
            remainingTime = payload.initialTime;
            expectedTime = Date.now() + remainingTime * 1000;

            timerInterval = setInterval(() => {
                const now = Date.now();
                const diff = expectedTime - now;

                if (diff <= 0) {
                    // Timer finished
                    self.postMessage({ type: "COMPLETE" });
                    if (timerInterval) clearInterval(timerInterval);
                    timerInterval = null;
                    remainingTime = 0;
                } else {
                    // Tick
                    const secondsLeft = Math.ceil(diff / 1000);
                    self.postMessage({ type: "TICK", payload: secondsLeft });
                    remainingTime = secondsLeft;
                }
            }, 100); // Check frequently for UI smoothness, though tick is per second roughly
            break;

        case "PAUSE":
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
                // Calculate exact remaining time
                remainingTime = Math.ceil((expectedTime - Date.now()) / 1000);
            }
            break;

        case "RESUME":
            if (timerInterval) return;
            // Recalculate expected time based on stored remainingTime
            expectedTime = Date.now() + remainingTime * 1000;
            timerInterval = setInterval(() => {
                const now = Date.now();
                const diff = expectedTime - now;

                if (diff <= 0) {
                    self.postMessage({ type: "COMPLETE" });
                    if (timerInterval) clearInterval(timerInterval);
                    timerInterval = null;
                    remainingTime = 0;
                } else {
                    const secondsLeft = Math.ceil(diff / 1000);
                    self.postMessage({ type: "TICK", payload: secondsLeft });
                }
            }, 100);
            break;

        case "RESET":
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
            remainingTime = payload.initialTime;
            self.postMessage({ type: "TICK", payload: remainingTime });
            break;
    }
};
