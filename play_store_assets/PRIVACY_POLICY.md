# Privacy Policy for MedVault / SnapPill

**Last Updated:** October 15, 2026

MedVault ("we", "our", or "app") is built with an absolute commitment to user privacy and medical confidentiality. This policy outlines how information is handled within the MedVault mobile application.

---

## 1. 100% Offline-First Architecture
MedVault operates entirely offline on your device.
- **No Remote Servers:** We do not operate remote servers, tracking services, or cloud databases that store your personal health information.
- **Local Storage:** All medication names, schedules, pill photos, adherence records, and profile details are saved exclusively in a private, encrypted local SQLite database on your device.

## 2. Information We Handle Locally
- **Medications & Schedules:** Drug names, dosages, frequencies, and instructions.
- **Visual Verification Photos:** Pill and prescription bottle photos captured via your device's camera.
- **Adherence History:** Timestamps of when doses were logged as taken or skipped.
- **Caregiver & Emergency Contacts:** Contact information entered for one-tap SMS safety pings.

## 3. Permissions Requested & Why
- **Camera (`android.permission.CAMERA`):** Used solely to capture photos of pills and prescription bottle labels for on-device visual verification and on-device text recognition. Images never leave your device.
- **Notifications (`android.permission.POST_NOTIFICATIONS`):** Used to display scheduled medication reminder alerts.
- **Vibration & Audio:** Used to sound reminder chimes and double-dose caution alarms.

## 4. Third-Party Sharing & Advertising
- **Zero Third-Party Data Sharing:** We do not sell, rent, monetize, or transmit your health information to pharmaceutical companies, data brokers, or advertisers.
- **No In-App Advertising:** MedVault contains zero ad SDKs or third-party trackers.

## 5. Doctor-Ready PDF Export
When you generate a Doctor-Ready Clinical Summary PDF, the document is compiled locally on your device using Android's native `PdfDocument` engine. The resulting file is shared only when you explicitly select a destination (such as printing or emailing your physician) via the standard Android system share sheet.

## 6. Children's Privacy
MedVault is intended for adults and family caregivers managing medication regimens. We do not knowingly collect personal information from children under 13.

## 7. Contact Us
For questions regarding this privacy policy or MedVault security practices, contact:
`privacy@medvaulthealth.app`
