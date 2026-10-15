# MedVault: Pill & Med Reminder 🛡️💊
**Google Play Store Production Ready • Android App Bundle (AAB)**

[![Google Play Ready](https://img.shields.io/badge/Google_Play-AAB_Ready-0F766E.svg)](play_store_assets/)
[![Target SDK](https://img.shields.io/badge/Target_SDK-35_(Android_15)-06B6D4.svg)](https://developer.android.com)
[![Signed Release](https://img.shields.io/badge/Build-Signed_Release-10B981.svg)](#play-store-signing--keystore)
[![Privacy First](https://img.shields.io/badge/Data_Safety-100%25_Offline-3B82F6.svg)](play_store_assets/PRIVACY_POLICY.md)

MedVault is a privacy-first, visual medication organizer & safety assistant built for Google Play Store publication. It protects seniors and caregivers from missed doses, look-alike pill errors, and accidental double-dosing.

---

## 📦 Google Play Store Submission Deliverables

| Deliverable | File Path | Description |
|---|---|---|
| **Android App Bundle (.aab)** | [`MedVault.aab`](MedVault.aab) | **23 MB** Signed Google Play bundle with R8 resource & code shrinking |
| **Standalone Release APK** | [`MedVault.apk`](MedVault.apk) | **44 MB** Signed release APK for side-loading & device testing |
| **App Store Icon** | [`play_store_assets/app_icon_512.png`](play_store_assets/app_icon_512.png) | 512 × 512 px (32-bit PNG) high-resolution store icon |
| **Feature Graphic** | [`play_store_assets/feature_graphic_1024x500.png`](play_store_assets/feature_graphic_1024x500.png) | 1024 × 500 px store promotion banner |
| **Store Listing Metadata** | [`play_store_assets/STORE_LISTING.md`](play_store_assets/STORE_LISTING.md) | Title, short description (80 chars), and full description (4000 chars) |
| **Privacy Policy** | [`play_store_assets/PRIVACY_POLICY.md`](play_store_assets/PRIVACY_POLICY.md) / [HTML](play_store_assets/privacy_policy.html) | Mandatory privacy policy for Google Play health & camera declaration |
| **Release Keystore** | [`keystore/release.keystore`](keystore/release.keystore) | RSA 2048-bit production keystore (valid for 10,000 days through 2053) |

---

## 🔑 Play Store Signing & Keystore Credentials

The release bundle is pre-configured and signed with:
- **Keystore Location**: `keystore/release.keystore`
- **Key Alias**: `medvault_playstore`
- **Keystore Password**: `MedVaultPlayStore2026!`
- **Key Password**: `MedVaultPlayStore2026!`
- **Validity**: 10,000 days (Google Play requirement: post-2050)

---

## 🚀 Building & Generating Bundles

### 1. Build Signed Android App Bundle (for Google Play Console Upload)
```bash
./gradlew bundleRelease
```
Artifact generated at `app/build/outputs/bundle/release/app-release.aab` (copied to root `MedVault.aab`).

### 2. Build Signed Release APK (for Direct Testing)
```bash
./gradlew assembleRelease
```
Artifact generated at `app/build/outputs/apk/release/app-release.apk` (copied to root `MedVault.apk`).

### 3. Install on Connected Device / Emulator
```bash
adb install -r MedVault.apk
adb shell am start -n com.medvault.snappill/.MainActivity
```

---

## 🌟 Google Play Feature Highlights

1. **🛡️ "Did I Take It?" Double-Check**: Real-time status evaluation of daily morning, afternoon, and evening prescriptions with 96% adherence tracking.
2. **⚠️ Accidental Double-Dose Interlock**: Halts attempts to log doses within the 4-hour safety buffer, sounding an acoustic alarm (`ToneGenerator`) and presenting an emergency amber caution dialog with elapsed time to prevent dangerous toxicity or hypotension.
3. **📸 Visual Pill Verification & CameraX**: Live camera feed to capture pills and bottles with verified imprint tags (e.g. *"L 10"*, *"M 500"*).
4. **🔍 On-Device Label OCR (ML Kit)**: Point camera at pill bottle labels to scan prescription information offline without third-party cloud data transmission.
5. **📄 Doctor-Ready 1-Page PDF Summary**: Generates a hospital-grade clinical adherence summary formatted for physician review via native `PdfDocument` and Android share sheet.
6. **👨‍👩‍👧 Caregiver Family Link**: Direct one-tap SMS alert generation (`Intent.ACTION_SENDTO`) to notify caregivers of unconfirmed doses.
7. **💊 Inventory Refill Alerts**: Decrements pill stock automatically, alerting when supply drops under 7 days with one-tap pharmacy calling.
8. **👁️ Senior Accessibility Mode**: High-contrast, 22sp+ typography with spoken Text-To-Speech announcements.
9. **💎 Pro Tier Monetization**: Free tier (up to 3 meds) vs Pro ($2.99/mo, $19.99/yr, $29.99 Lifetime) with evaluation test unlock toggle.
