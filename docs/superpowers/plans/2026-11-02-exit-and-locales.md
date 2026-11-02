# Exit and Locales Implementation Plan

**Goal:** Complete on editor back / user departure, add a gentle exit, support en / zh-Hant / ja in 0.1.16 / code 17.

**Architecture:** Reuse guarded copy/finish. Editor BackHandler preserves modal handling. onUserLeaveHint completes modified sessions, not generic onPause/focus loss; suppress tile-request interruptions. Done/back animate translucent decor for 360ms on API30+, native transition on API29. Home animation belongs to the launcher. Complete locale overlays and localeConfig.

- [x] Implement completion, restored baseline, back / user departure and animation; keep copy-failure draft.
- [x] Translate all strings and declare supported locales.
- [x] Build / lint / unit / device checks for exact copy, back, Home, interruption, modal and locale layout.
- [x] Sign, document, verify and commit locally; no automatic publication requested.
