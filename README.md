# PRD v0.3 — AKQUA Gel Demo (Android Native)

## 0) 목표

* **센서 연동 없음**(BT 없음). 모든 센서값은 더미/시뮬레이션.
* **카메라는 실제 촬영** 사용.
* 촬영된 사진에서 **젤 색상(노랑/연노랑/투명)**을 “판별하는 것처럼” 보여주되:

  * **정밀 분석/ML 금지**
  * 단순한 **색상 휴리스틱(평균 색/HSV 구간)**만 허용
  * 데모 안전을 위해 **수동 Stage 오버라이드(숨김 옵션)**를 넣어도 좋음(선택)

## 1) 핵심 시나리오

### [1] Live Monitoring : 모니터링&측정 (실시간 측정 인상)

* “실시간 측정 중” 인상
* Temperature / Humidity / Impedance 카드
* 값이 **천천히 변하는 애니메이션**
* 하단 문구: `Sensor data is being continuously collected.`

### [2] Healing Stage Screen (핵심 화면 – 3단계)

* 사진을 찍고, **색깔에 따라 stage를 선택**(휴리스틱)
* 센서 데이터(더미) + 해석 메시지 + CTA 표시
  * stage 별 예시 화면도 추가 (데모 안전을 위해 수동 stage 오버라이드) 
* 3단계:
  * 🔴 Stage 1: Wound Detected (Yellow, 38.1°C, 82%, 420Ω)
  * 🟡 Stage 2: Healing in Progress (Light Yellow, 36.9°C, 65%, 610Ω)
  * 🟢 Stage 3: Healing Completed (Transparent, 36.4°C, 48%, 820Ω)

### [3] Data Report : PDF Export + Share

* [1],[2]에서 나온 데이터를 요약한 PDF 생성
* `Share` 버튼으로 의사에게 공유하는 플로우 (Android share sheet)

## 2) Out of Scope (명확히 금지)

* 로그인/회원가입
* Bluetooth/센서 연동
* 서버/네트워크/클라우드 저장
* ML/CV 모델 기반 분석
* 의료 정확성 보장

## 3) 앱 내 데이터 모델(권장)

* `MonitoringSample(timestamp, temperature, humidity, impedance)`
* `HealingStage(enum: STAGE_1, STAGE_2, STAGE_3)`
* `StageResult(stage, gelColorLabel, message, cta, temp, humidity, impedance, photoUri, detectedAt)`
* `ReportData(monitoringWindow: List<MonitoringSample>, stageResult: StageResult)`

---

# 화면/플로우 제안 (Navigation)

**Start → LiveMonitoring → HealingStage(Camera) → StageResult → Report(PDF)**

* `live_monitor`
* `camera_capture`
* `stage_result`
* `report_preview` (선택: 미리보기/생성)
* 또는 `stage_result`에서 바로 PDF 생성+Share

---

# Android Native Step-by-step Codex/Cursor 프롬프트

아래 프롬프트를 **Step 0부터 하나씩** 실행한다.
매 스텝 프롬프트 맨 위에는 항상 이 “워크플로 규칙”을 붙여주도록 한다. (충돌 방지)

## 공통 워크플로 규칙 (매번 붙여넣기)

```text
Repository workflow rules (must follow):
- Work ONLY on the existing branch: feature/demo-prototype (do NOT create new branches).
- Do NOT create new PRs. Keep pushing commits to the same branch.
- Make minimal diffs: modify only the files necessary for this step.
- Do NOT reformat or rewrite unrelated files.
- Patch existing files; never recreate files unless explicitly requested.
- Must be Android Native (Kotlin).
Must NOT implement: login/auth, bluetooth, backend/network, ML/CV analysis.
Camera is real; sensor values are simulated dummy only.
At the end: list exact files changed.
```

---

## Step 0 — 프로젝트 세팅 (Compose + Nav + CameraX + Coil)

```text
Set up a minimal runnable Android app with:
- Kotlin + Jetpack Compose (Material3)
- Navigation-Compose
- CameraX (camera-camera2, camera-lifecycle, camera-view, camera-core)
- Coil (coil-compose)

Tasks:
1) Ensure minSdk 24+.
2) Enable Compose build features.
3) Add dependencies above.
4) Add AndroidManifest permissions: CAMERA.
5) Create MainActivity with setContent { AppRoot() }.

Deliverables:
- Gradle updates
- AndroidManifest updates
- MainActivity + AppRoot stub
```

---

## Step 1 — Navigation + 3개 화면 뼈대 만들기

(우선 시나리오 핵심만: LiveMonitoring / CameraCapture / StageResult)

```text
Implement navigation with 3 routes:
- live_monitor
- camera_capture
- stage_result?photoUri=...

Create composable screens:
1) LiveMonitoringScreen: placeholder UI
2) CameraCaptureScreen: placeholder UI
3) StageResultScreen: placeholder UI

Add a shared ViewModel (NavGraph scoped) to store:
- lastMonitoringSample (or list)
- lastStageResult (optional)
- photoUri

LiveMonitoringScreen has a button "Go to Healing Stage" -> camera_capture
CameraCaptureScreen has a temp button "Capture (mock)" -> navigate to stage_result with a dummy uri for now

Deliverables:
- AppRoot NavHost
- Screens + ViewModel skeleton
```

---

## Step 2 — [1] Live Monitoring UI + 느리게 변하는 애니메이션 값

```text
Implement LiveMonitoringScreen for Scenario [1].

UI:
- Header: "Live Monitoring"
- Subheader text: "Sensor data is being continuously collected."
- 3 cards: Temperature, Humidity, Impedance
Each card shows a numeric value that slowly changes over time.

Behavior:
- Use a coroutine in ViewModel to emit new values every 1s (or 2s).
- Use smooth animation in Compose (animateFloatAsState or Animatable) to interpolate between old/new values.
- Keep values within realistic ranges:
  Temperature: 36.0 - 38.5
  Humidity: 40 - 85
  Impedance: 350 - 900

Deliverables:
- LiveMonitoringScreen complete with animated numbers
- ViewModel generates simulated monitoring stream
No charts, no network.
```

---

## Step 3 — Camera 권한 + CameraX 프리뷰 + 촬영 저장(실제)

```text
Implement CameraCaptureScreen with real CameraX.

Requirements:
- Request CAMERA permission using ActivityResultContracts.RequestPermission.
- If granted, show live PreviewView via AndroidView.
- Add capture button overlay.
- On capture, save image to app-specific storage (cacheDir or externalFilesDir).
- Obtain a Uri string and navigate to stage_result passing photoUri.
- Handle errors with a message.

Constraints:
- No gallery picker.
- No ML analysis.

Deliverables:
- Real capture works on device
- Navigates to stage_result with real photoUri
- Add FileProvider only if needed for sharing later; keep minimal.
```

---

## Step 4 — [2] Healing Stage 판별 로직 (휴리스틱) + StageResultScreen 완성

```text
Implement Scenario [2] StageResultScreen.

Inputs:
- photoUri (String) from nav arg

Goal:
- Display 3-part layout:
  Top: Stage Title
  Middle: status icon + stage color indicator
  Bottom: interpretation message + CTA button
  Also show Monitoring values and "Color of Gel" label.

Stage definitions:
Stage 1 (RED): "Wound Detected"
- Message: "Signs of early inflammation detected."
- CTA: "Increase monitoring & consider care intervention"
- Gel color label: "Yellow"
- Temp 38.1, Humidity 82, Impedance 420

Stage 2 (YELLOW): "Healing in Progress"
- Message: "Wound is healing as expected."
- CTA: "Maintain current care routine"
- Gel color label: "Light Yellow"
- Temp 36.9, Humidity 65, Impedance 610

Stage 3 (GREEN): "Healing Completed"
- Message: "Healing nearly complete."
- CTA: "Reduce monitoring & resume normal care"
- Gel color label: "Transparent color"
- Temp 36.4, Humidity 48, Impedance 820

Color detection (must be simple heuristic, NOT ML):
- Load bitmap from photoUri (downsample).
- Compute average color or average HSV of central crop.
- Map to one of:
  - Yellow -> Stage 1
  - Light Yellow -> Stage 2
  - Very low saturation / high value -> Transparent -> Stage 3
If detection fails, default to Stage 2 (safe demo default).

Also:
- Show the captured photo at top (thumbnail) using Coil.

Deliverables:
- StageResultScreen complete
- Utility function detectGelStage(photoUri): HealingStage using basic average color/HSV
- StageResult data class stored in ViewModel
```

> 팁: “투명”은 현실적으로 사진에서 판별이 애매할 수 있어서
> **fail-safe 기본값을 Stage 2로** 두는 게 데모 안정성 최고야.

---

## Step 5 — [3] PDF Report 생성 (Android PdfDocument) + Share Sheet

```text
Implement Scenario [3]: PDF export and share.

UI changes:
- Add a button on StageResultScreen: "Export PDF Report"
- After generating, show a snackbar "PDF created" and a "Share" button (or directly open share sheet).

PDF requirements:
- Use android.graphics.pdf.PdfDocument (no external PDF libraries).
- 1-page PDF is enough.
- Include:
  - Title: "AKQUA Gel Data Report"
  - Date/time
  - Stage title + message + CTA
  - Monitoring values (Temp/Humidity/Impedance)
  - Gel color label
  - (Optional) small photo thumbnail if easy; if not, omit to keep stable.

Storage:
- Save PDF under app-specific files (e.g., getExternalFilesDir(DIRECTORY_DOCUMENTS) or cacheDir).
- Use FileProvider to share the PDF via ACTION_SEND with correct MIME "application/pdf".
- Ensure granting URI permissions.

Deliverables:
- PdfReportGenerator helper class
- FileProvider setup in manifest + xml paths if needed
- Working share flow opens Android share sheet
No network.
```

---

## Step 6 — 데모 안정성(Back stack, 예외 처리, 리셋)

```text
Hardening pass for demo reliability.

Tasks:
- Ensure back navigation is natural:
  live_monitor -> camera_capture -> stage_result -> (share) then back to stage_result
- Add "Retake Photo" button on StageResultScreen to go back to camera_capture.
- Add "Reset Demo" button on StageResultScreen to clear ViewModel state and go to live_monitor.
- Improve error handling:
  - If photoUri invalid: show message and provide "Retake".
- Keep code minimal and clean.

Deliverables:
- Navigation refinements
- State reset
- Basic error UX
```
