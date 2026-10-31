# Glass Material Remake Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the heavy gradient glass with curved optical materials and local fluid touch feedback in both glass modes.

**Architecture:** Separate AGSL scene / optical programs from Compose surface and interaction code. Derive a smooth 3D bevel normal from rounded-rectangle distance, use Snell transmission and angle-dependent Fresnel reflection in linear light, and render tight directional highlights. Floating mode uses the same surface optics with transparent transmission over the system-composited app; it cannot sample another app's pixels.

**Tech Stack:** Existing Compose / AGSL / Canvas / public window blur APIs; no added dependency or permission.

**Spec:** docs/liquid-glass-design.md, docs/spec.md, current user request.

## Constraints

- Preserve Material, floating toggle, text, undo, tile state, keyboard avoidance and copy / exit.
- Glass stays in the control layer; the document remains a reading surface.
- No screenshots or MediaProjection in production, no hidden APIs or simulated cross-app pixel refraction claims.
- Each touch response is local; no shared button interaction source or group press response.
- No permanent animation loop, stable layout during IME, minimum 48dp targets.
- Ship signed versionCode 10 / versionName 0.1.9.

### Task 1: Optical material and composition

**Files:** rewrite ui/LiquidGlass.kt; new ui/GlassOpticalPrograms.kt; FloatingWindowController.kt; EditorChrome.kt.

- [x] Implement an elliptical bevel height profile and surface normal, Snell ray displacement, Schlick Fresnel, restrained absorption and directional environment light; calculate lighting in linear sRGB.
- [x] Keep ordinary scene transmission opaque and floating transmission premultiplied / translucent. Remove wide uniform white rims and separate gradient-only floating material.
- [x] Use a quieter scene and finer shadow; reduce whole-window blur and replace the large floating metadata slab with a content-sized inset.
- [x] Add local soft light / elastic feedback per glass button, retaining existing native feedback for Material and accessibility labels.
- [x] Build and inspect material over a high-contrast grid before final tuning.

### Task 2: Verification and delivery

**Files:** debug-only GlassMaterialProbeActivity / manifest; androidTest material rendering check; existing test suites; validation docs and artifacts.

- [x] Render the same shared diagnostic scene beneath the lens and in an unmodified reference strip; retain native screenshots showing edge displacement and unchanged flat-center geometry.
- [x] Verify local press response and sharp foreground content, then inspect ordinary / floating and light / dark on the emulator.
- [x] Run unit, lint, release builds, the existing 12 device regressions and 3 animated floating caret checks.
- [x] Sign and cover-install release, inspect over a real system app, save before / after previews and logs, update research notes with implementation limits and deliver APK.

Execute inline under existing authorization. No Git repository is present.
