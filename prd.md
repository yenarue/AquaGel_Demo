# AKQUA Gel Demo App — PRD (Android Native)

- Version: v0.4  
- Purpose: Demo-only prototype for live presentation  
= Platform: Android Native (Kotlin + Jetpack Compose)

---

## 1. Overview

AKQUA Gel Demo App is a **demo-only Android application** designed to showcase the concept of a smart wound-monitoring hydrogel.

This app focuses on:
- Visual storytelling
- Realistic interaction flow
- Clear demonstration of product value

⚠️ This is NOT a production app and NOT a medical device.

---

## 2. Demo Goals

The demo must clearly communicate the following within **3 minutes**:
1. Sensor data appears to be monitored continuously
2. Wound healing stage can be interpreted visually
3. Data can be summarized and shared with a doctor

---

## 3. Core Scenarios

### [Scenario 1] Live Monitoring (Monitoring & Measurement)

**Goal:**  
Create a strong impression of *real-time sensor monitoring*.

**UI Requirements**
- Header: `Live Monitoring`
- Animated status text: `실시간 측정 중`
- Subtitle:
```

Sensor data is being continuously collected.

```
- Monitoring Data Cards (3):
- Temperature (°C)
- Humidity (%)
- Impedance (Ω)

**Behavior**
- Values change slowly over time (simulated)
- Smooth animation between value changes
- No charts required (cards only)

**Data**
- Temperature: 36.0 – 38.5 °C
- Humidity: 40 – 85 %
- Impedance: 350 – 900 Ω

All data is **dummy and simulated**.

---

### [Scenario 2] Healing Stage Screen (Core Screen – 3 Stages)

**Goal:**  
Demonstrate how wound condition can be interpreted using:
- Visual appearance (gel color)
- Sensor data (dummy)

This is the **main value screen** of the demo.

---

#### Flow
1. User takes a photo of the wound (real camera)
2. App determines healing stage based on gel color (simple heuristic)
3. Stage-specific message, color, and CTA are displayed

---

#### Layout Structure

- **Top**
- Stage Title
- **Center**
- Status Icon
- Stage Color Indicator
- **Bottom**
- Interpretation Message
- CTA Button
- **Additional**
- Monitoring data
- Gel color label
- Photo thumbnail

---

#### Stage Definitions

##### 🔴 Stage 1 — Wound Detected
- Message:
```

Signs of early inflammation detected.

```
- CTA:
```

Increase monitoring & consider care intervention

```
- Gel Color: Yellow
- Monitoring:
- Temp: 38.1°C
- Humidity: 82%
- Impedance: 420Ω

---

##### 🟡 Stage 2 — Healing in Progress
- Message:
```

Wound is healing as expected.

```
- CTA:
```

Maintain current care routine

```
- Gel Color: Light Yellow
- Monitoring:
- Temp: 36.9°C
- Humidity: 65%
- Impedance: 610Ω

---

##### 🟢 Stage 3 — Healing Completed
- Message:
```

Healing nearly complete.

```
- CTA:
```

Reduce monitoring & resume normal care

```
- Gel Color: Transparent
- Monitoring:
- Temp: 36.4°C
- Humidity: 48%
- Impedance: 820Ω

---


---

#### Demo Safety: Manual Stage Override & Example Screens

To ensure **demo stability and predictability**, the app must support a **manual stage override** feature.

**Purpose**
- Prevent demo failure due to lighting, photo quality, or ambiguous gel color
- Allow presenter to intentionally showcase each healing stage

**Requirements**
- Provide a hidden or secondary control (e.g. overflow menu, long-press, debug button) to manually select:
    - Stage 1 — Wound Detected
    - Stage 2 — Healing in Progress
    - Stage 3 — Healing Completed
- Manual override must:
    - Bypass color heuristic
    - Immediately update UI, messages, CTA, and monitoring values
    - Be visually indistinguishable from automatic detection during demo

**Example Stage Screens**
- Include clearly designed example screens for each stage:
    - 🔴 Stage 1 example (Yellow gel, high temp/humidity, low impedance)
    - 🟡 Stage 2 example (Light yellow gel, mid-range values)
    - 🟢 Stage 3 example (Transparent gel, stable values)
- These example screens may reuse the same StageResult UI with forced stage data.

**Priority**
- Manual override is **high priority for demo safety**
- Automatic detection is preferred, but override must always be available



#### Stage Detection Rule

- Use **simple image heuristic only**
- Allowed:
- Average color
- HSV analysis
- Central crop
- NOT allowed:
- ML / AI / CV models

Fail-safe rule:
- If detection is ambiguous → default to **Stage 2**

---

### [Scenario 3] Data Report (PDF Export & Share)

**Goal:**  
Demonstrate how collected data can be summarized and shared with a doctor.

**Requirements**
- Generate a **1-page PDF**
- Include:
- Title: `AKQUA Gel Data Report`
- Date / Time
- Healing Stage
- Interpretation message
- Monitoring values
- Gel color label
- Optional:
- Small photo thumbnail (if stable)

**Share**
- Use Android share sheet (`ACTION_SEND`)
- MIME type: `application/pdf`
- FileProvider required

---

## 4. App Flow & Navigation

```

Live Monitoring
↓
Camera Capture
↓
Healing Stage Result
↓
Export / Share PDF

````

Routes (example):
- `live_monitor`
- `camera_capture`
- `stage_result`
- `report_export` (optional)

---

## 5. Data Models (Recommended)

```kotlin
MonitoringSample(
  timestamp: Long,
  temperature: Float,
  humidity: Float,
  impedance: Int
)

enum class HealingStage {
  STAGE_1, STAGE_2, STAGE_3
}

StageResult(
  stage: HealingStage,
  gelColorLabel: String,
  message: String,
  cta: String,
  temperature: Float,
  humidity: Float,
  impedance: Int,
  photoUri: String,
  detectedAt: Long
)

ReportData(
  monitoringSamples: List<MonitoringSample>,
  stageResult: StageResult
)
````

---

## 6. Technical Constraints

### Must Use

* Kotlin
* Jetpack Compose (Material3)
* Navigation-Compose
* CameraX (real camera)
* PdfDocument (Android SDK)
* Coil for image display

### Must NOT Use

* Login / Auth
* Bluetooth
* Network / Backend
* Cloud storage
* ML / CV / AI models
* External PDF libraries

---

## 7. Demo Stability Rules

* App must be fully usable offline
* No crashes during normal navigation
* Back navigation must feel natural
* Provide:

    * `Retake Photo`
    * `Reset Demo`

---

## 8. Definition of Done (Demo)

* Real camera capture works
* Live monitoring feels “alive”
* Healing stage screen clearly communicates value
* PDF can be generated and shared
* Remains understandable without explanation

---

## 9. Implementation TODO List

### Phase 0 — Setup

* [ ] Android project setup (Compose, Nav, CameraX)
* [ ] Manifest permissions
* [ ] FileProvider base config

### Phase 1 — Live Monitoring

* [ ] Monitoring ViewModel (simulated stream)
* [ ] Animated value cards (Temp/Humidity/Impedance)
* [ ] UI polish for “real-time” impression

### Phase 2 — Camera & Stage Detection

* [ ] Camera permission flow
* [ ] CameraX preview
* [ ] Photo capture & local storage
* [ ] Basic color heuristic for stage detection
* [ ] StageResult screen UI

### Phase 3 — PDF & Share

* [ ] PDF generation helper
* [ ] Report layout in PDF
* [ ] Share intent integration

### Phase 4 — Demo Hardening

* [ ] Back stack verification
* [ ] Error handling
* [ ] Reset demo flow
* [ ] Minor UI polish

---

## 10. Codex / Cursor Instruction

When implementing:

* Read this `prd.md` fully before coding
* Follow scope strictly
* Modify only required files
* Do not introduce new features
* Prefer clarity and demo stability over completeness
