# MedVault User Guide & Google Play Conformance Audit 📖🛡️

Welcome to **MedVault (SnapPill)**, your privacy-first, visual medication organizer & pill safety assistant.

---

## 📑 TABLE OF CONTENTS
1. [Quick Start Guide](#1-quick-start-guide)
2. [How the "Did I Take It?" Double-Check Works](#2-how-the-did-i-take-it-double-check-works)
3. [Understanding the Accidental Double-Dose Protection Interlock](#3-understanding-the-accidental-double-dose-protection-interlock)
4. [Visual Pill Verification & Imprint Matching](#4-visual-pill-verification--imprint-matching)
5. [Scanning Prescription Labels with Camera OCR](#5-scanning-prescription-labels-with-camera-ocr)
6. [Generating Doctor-Ready 1-Page PDF Summaries](#6-generating-doctor-ready-1-page-pdf-summaries)
7. [Family Link & Caregiver Missed-Dose SMS Alerts](#7-family-link--caregiver-missed-dose-sms-alerts)
8. [Refill Inventory Management & Pharmacy Ordering](#8-refill-inventory-management--pharmacy-ordering)
9. [Senior Accessibility Mode & Spoken Voice Assistant](#9-senior-accessibility-mode--spoken-voice-assistant)
10. [Google Play Store Conformance Audit](#10-google-play-store-conformance-audit)
11. [Assistive UI/UX & Accessibility Audit for Physically Challenged Users](#11-assistive-uiux--accessibility-audit-for-physically-challenged-users)

---

## 1. Quick Start Guide

When you launch MedVault, your dashboard immediately loads 3 active prescriptions pre-configured for demonstration:
- **Lisinopril 10 mg** (Blood Pressure • Morning dose at 08:00 AM)
- **Metformin HCl 500 mg** (Type 2 Diabetes • Morning 08:00 AM & Evening 07:00 PM)
- **Atorvastatin 20 mg** (Cholesterol • Evening 09:00 PM • Refill Alert active)

### Logging a Dose
1. Scroll down to your medication card.
2. Tap the large green **"Log Taken"** button.
3. You will hear an acoustic success chime, a spoken confirmation (*"Confirmed. Lisinopril 10 milligrams recorded"*), and see an instant checkmark badge updating the time taken today.

---

## 2. How the "Did I Take It?" Double-Check Works

Located prominently at the top of your screen, the **Did I Take It?** card is your persistent truth center:
- **Real-Time Summary**: Immediately displays which doses were confirmed today and which dose is due next.
- **30-Day Adherence Metric**: Highlights your adherence score (e.g. `96% Adherence`) calculated from your on-device SQLite records.
- **Protection Lock Status**: Displays `🛡️ Accidental Double-Dose Lock: ACTIVE`.

---

## 3. Understanding the Accidental Double-Dose Protection Interlock

Accidental double-dosing is one of the leading causes of preventable emergency room admissions among seniors.

### What Triggers the Interlock?
If you or a caregiver tap **"Take Again"** on a medication that has already been taken within its clinical safety window (default: 4 hours):
1. **Acoustic Warning**: The app sounds an emergency CDMA warning tone (`ToneGenerator`).
2. **Spoken Announcement**: The voice assistant warns: *"Warning. You already took your Lisinopril dose 25 minutes ago."*
3. **Screen Reader Announcement**: TalkBack announces the safety alert immediately.
4. **Amber Caution Dialog**: A prominent warning modal appears detailing:
   - The exact medication and dosage.
   - The precise timestamp when the dose was previously logged.
   - Clinical risk guidance (hypotension, toxicity, adverse drug interactions).
   - Prominent **"Stay Safe"** button (cancels and dismisses) and a secondary **"Take Anyway"** button (for intentional physician-directed overrides).

---

## 4. Visual Pill Verification & Imprint Matching

Never mix up look-alike pills again.
1. On any medication card, tap **"Verify Pill"**.
2. A high-resolution dialog opens showing:
   - Tablet shape (Round, Capsule, Oval, Square, Drop, Inhaler, Injection).
   - Custom pill color avatar.
   - **Verified Imprint Overlay** (e.g., *"L 10"*, *"M 500"*).
   - Food and liquid instructions (e.g., *"Take with full glass of water"*).
3. Tap **"Photograph My Actual Pill / Bottle"** to launch CameraX, photograph your personal prescription, and save it as your custom visual avatar.

---

## 5. Scanning Prescription Labels with Camera OCR

Avoid manual typing errors:
1. Tap the floating **Cyan Camera FAB** (`fabScanBottle`) in the bottom right corner.
2. Point your camera at your prescription bottle label.
3. On-device Google ML Kit Text Recognition scans the text to pre-populate drug name, dosage, instructions, and Rx numbers.

---

## 6. Generating Doctor-Ready 1-Page PDF Summaries

Physicians need accurate adherence records, not vague recollections:
1. Tap the **"Doctor PDF"** button on the top action bar.
2. MedVault uses Android's native `PdfDocument` engine to render a clean, hospital-grade A4 clinical summary:
   - Patient Demographics (Name, DOB, Blood Type, Primary Care Physician).
   - **Bold Red Drug Allergies** (e.g. *Penicillin, Sulfa drugs*).
   - 30-Day Logged Adherence Percentage.
   - Complete Prescription Table (Drug name, Brand, Dosage, Schedule, Indications, Rx number).
   - Pharmacy verification & refill statuses.
   - Attending Physician Signature and Date/Clinic Stamp lines.
3. Automatically opens Android's native share sheet to **Print**, **Email**, or **Export** directly to your doctor's office.

---

## 7. Family Link & Caregiver Missed-Dose SMS Alerts

1. Tap the **Caregiver Icon** (top header bar).
2. View your designated family caregiver contact (e.g., *David Vance (Son) • +1 (555) 432-1987*).
3. Tap **"Send Missed-Dose SMS Alert"** to launch your native messaging app with a pre-composed message:
   > *"MedVault Caregiver Alert: Eleanor Vance medication check-in. Morning Lisinopril 10mg logged. Please confirm evening doses."*
4. Tap **"Call Caregiver Directly"** to dial the caregiver immediately.

---

## 8. Refill Inventory Management & Pharmacy Ordering

1. Tap the **"Refills"** button on the action bar.
2. Any medication with supply under 7 days is automatically flagged with an amber warning banner.
3. Tap **"Call Pharmacy for Refill"** to dial your pharmacy prescription hotline directly (`Intent.ACTION_DIAL`).
4. When you pick up your medication, tap **"✓ I Picked Up Refill (+30 Pills)"** to instantly restock inventory and clear the warning badge.

---

## 9. Senior Accessibility Mode & Spoken Voice Assistant

Designed for seniors and users with visual, hearing, or motor impairments:
1. Tap the **Eye Icon** (`btnSeniorMode`) in the top bar.
2. Instantly toggles **Senior Mode**:
   - Enlarges typography to **20sp–24sp** with bold contrast.
   - Spoken dosage announcements: automatically reads instructions aloud using Android `TextToSpeech`.
   - Sound chimes for all button actions.

---

## 10. Google Play Store Conformance Audit

| Audit Area | Google Play Requirement | MedVault Conformance Status | Technical Verification |
|---|---|---|---|
| **API Level** | Target SDK 34+ (Android 14+) | ✅ **Target SDK 35 (Android 15)** | `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` in `app/build.gradle.kts`. |
| **Packaging** | Android App Bundle (.aab) | ✅ **Compliant (`MedVault.aab`)** | 23 MB signed App Bundle with R8 optimization. |
| **Signing** | Production Keystore | ✅ **Compliant (`release.keystore`)** | RSA 2048-bit keystore with 10,000-day validity through 2053. |
| **Store Icon** | 512 × 512 px 32-bit PNG | ✅ **Compliant (`app_icon_512.png`)** | Generated in `play_store_assets/` without alpha corners. |
| **Feature Graphic** | 1024 × 500 px JPEG/PNG | ✅ **Compliant (`feature_graphic_1024x500.png`)** | Generated in `play_store_assets/`. |
| **Privacy Policy** | Mandatory for health/camera | ✅ **Compliant (`PRIVACY_POLICY.md` / `privacy_policy.html`)** | Discloses 100% on-device processing, zero third-party tracking. |
| **Permissions** | Camera hardware required=false | ✅ **Compliant** | `<uses-feature android:name="android.hardware.camera" android:required="false" />`. |
| **Backup Rules** | Android 12+ dataExtractionRules | ✅ **Compliant** | `data_extraction_rules.xml` and `backup_rules.xml` configured. |

---

## 11. Assistive UI/UX & Accessibility Audit for Physically Challenged Users

MedVault was audited against the **W3C WCAG 2.1 Level AA/AAA** and **Google Material Accessibility Guidelines**:

### A. Motor Disabilities & Tremors / Arthritis
- **Touch Target Sizing**: All interactive buttons (`btnTakeDose`, `btnVerifyPill`, close buttons, dialog actions) meet or exceed the **48 × 48 dp** minimum touch target requirement to prevent accidental mis-taps.
- **Zero Complex Gestures**: No required pinches, multi-finger swipes, or rapid double-taps. All workflows are achievable through simple, single taps.
- **Accidental Click Reversal**: Includes an instant **"Undo"** snackbar action when logging doses.

### B. Visual Impairments & Low Vision
- **High-Contrast Dark Palette**: Deep slate navy (`#090D16`) with high-contrast text (`#F8FAFC`, contrast ratio > 12:1, exceeding WCAG AAA 7:1 standard).
- **Senior Mode 1-Tap Scaling**: Instantly enlarges header and dashboard text to 20sp–24sp.
- **Spoken Text-To-Speech**: Built-in `SpeechAssistant` reads medication names and instructions aloud at an optimized senior cadence (0.92x rate).
- **Non-Color Dependent Indicators**: Every status pairs color with distinct symbols (✓ checkmarks, ⚠️ warning triangles, ⏰ clocks) so color-blind users are never confused.

### C. Screen Reader (TalkBack) Compliance
- **Content Descriptions**: Every icon, image view, and action element includes descriptive, localized `android:contentDescription` attributes (e.g. *"Close Pill Verification"*, *"Caregiver Link"*).
- **Live Accessibility Announcements**: Calls `announceForAccessibility(...)` when critical events occur (e.g. *"Warning: Potential Double-Dose"*, *"Verified Taken: Lisinopril 10 mg confirmed"*), ensuring blind users receive immediate spoken feedback.

### D. Hearing Impairments
- All acoustic chime events are paired with synchronous visual feedback (snackbars, toast messages, and color badge state changes).
