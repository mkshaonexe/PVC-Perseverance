/* eslint-disable no-restricted-globals */
// Web Worker for accurate timing

let timerInterval: ReturnType<typeof setInterval> | null = null;
let expectedTime: number = 0;
let remainingTime: number = 0;
let lastTickSecond: number = -1; // Track last second we sent a tick for

self.onmessage = (e: MessageEvent) => {
    const { type, payload } = e.data;

    switch (type) {
        case "START":
            if (timerInterval) {
                clearInterval(timerInterval);
            }
            remainingTime = payload.initialTime;
            expectedTime = Date.now() + remainingTime * 1000;
            lastTickSecond = remainingTime;

            // Send initial tick
            self.postMessage({ type: "TICK", payload: remainingTime });

            timerInterval = setInterval(() => {
                const now = Date.now();
                const diff = expectedTime - now;

                if (diff <= 0) {
                    // Timer finished
                    self.postMessage({ type: "COMPLETE" });
                    if (timerInterval) clearInterval(timerInterval);
                    timerInterval = null;
                    remainingTime = 0;
                    lastTickSecond = -1;
                } else {
                    const secondsLeft = Math.ceil(diff / 1000);
                    // Only send tick when second actually changes
                    if (secondsLeft !== lastTickSecond) {
                        self.postMessage({ type: "TICK", payload: secondsLeft });
                        lastTickSecond = secondsLeft;
                    }
                    remainingTime = secondsLeft;
                }
            }, 100);
            break;

        case "PAUSE":
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
                // Calculate exact remaining time
                remainingTime = Math.max(0, Math.ceil((expectedTime - Date.now()) / 1000));
            }
            break;

        case "RESUME":
            if (timerInterval) return;
            // Use payload if provided, otherwise use internal state
            if (payload && payload.initialTime !== undefined) {
                remainingTime = payload.initialTime;
            }
            expectedTime = Date.now() + remainingTime * 1000;
            lastTickSecond = remainingTime;

            timerInterval = setInterval(() => {
                const now = Date.now();
                const diff = expectedTime - now;

                if (diff <= 0) {
                    self.postMessage({ type: "COMPLETE" });
                    if (timerInterval) clearInterval(timerInterval);
                    timerInterval = null;
                    remainingTime = 0;
                    lastTickSecond = -1;
                } else {
                    const secondsLeft = Math.ceil(diff / 1000);
                    if (secondsLeft !== lastTickSecond) {
                        self.postMessage({ type: "TICK", payload: secondsLeft });
                        lastTickSecond = secondsLeft;
                    }
                    remainingTime = secondsLeft;
                }
            }, 100);
            break;

        case "RESET":
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
            remainingTime = payload.initialTime;
            lastTickSecond = remainingTime;
            self.postMessage({ type: "TICK", payload: remainingTime });
            break;
    }
};
