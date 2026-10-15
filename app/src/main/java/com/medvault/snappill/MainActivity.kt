package com.medvault.snappill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.medvault.snappill.data.db.MedVaultDbHelper
import com.medvault.snappill.data.model.DoseLog
import com.medvault.snappill.data.model.Medication
import com.medvault.snappill.data.model.PatientProfile
import com.medvault.snappill.databinding.ActivityMainBinding
import com.medvault.snappill.ui.MedicationAdapter
import com.medvault.snappill.util.PdfReportGenerator
import com.medvault.snappill.util.SoundEffects
import com.medvault.snappill.util.SpeechAssistant
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: MedVaultDbHelper
    private lateinit var adapter: MedicationAdapter
    private lateinit var speechAssistant: SpeechAssistant
    private lateinit var cameraExecutor: ExecutorService

    private var profile = PatientProfile()
    private var imageCapture: ImageCapture? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Camera access needed for visual pill photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MedVaultDbHelper(this)
        speechAssistant = SpeechAssistant(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        profile = dbHelper.getPatientProfile()

        setupRecyclerView()
        setupListeners()
        updateDashboard()
    }

    private fun setupRecyclerView() {
        val meds = dbHelper.getAllMedications()
        adapter = MedicationAdapter(
            context = this,
            medications = meds,
            dbHelper = dbHelper,
            onTakeDose = { med -> handleTakeDoseAttempt(med) },
            onVerifyPill = { med -> showVisualPillVerifyDialog(med) }
        )
        binding.recyclerMedications.layoutManager = LinearLayoutManager(this)
        binding.recyclerMedications.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSeniorMode.setOnClickListener {
            toggleSeniorMode()
        }

        binding.btnCaregiverSos.setOnClickListener {
            showCaregiverDialog()
        }

        binding.chipProBadge.setOnClickListener {
            showProUpgradeDialog()
        }

        binding.btnExportDoctorPdf.setOnClickListener {
            exportDoctorPdfReport()
        }

        binding.btnRefillAlerts.setOnClickListener {
            showRefillTrackerDialog()
        }

        binding.fabAddMedication.setOnClickListener {
            showAddMedicationDialog()
        }

        binding.fabScanBottle.setOnClickListener {
            checkCameraPermissionAndScanBottle()
        }
    }

    private fun handleTakeDoseAttempt(med: Medication) {
        val now = System.currentTimeMillis()
        val lastLog = dbHelper.getLastLogForMedication(med.id)

        if (lastLog != null) {
            val elapsedMs = now - lastLog.takenTimestamp
            val elapsedHours = elapsedMs / (1000 * 60 * 60.0)

            if (elapsedHours < med.safetyBufferHours) {
                val elapsedMinutes = (elapsedMs / (1000 * 60)).toInt()
                val elapsedFormatted = if (elapsedMinutes >= 60) {
                    "${elapsedMinutes / 60} hour(s) and ${elapsedMinutes % 60} minute(s)"
                } else {
                    "$elapsedMinutes minute(s)"
                }

                SoundEffects.playDoubleDoseWarning()
                if (profile.voiceEnabled) {
                    speechAssistant.speak("Warning. You already took your ${med.name} dose $elapsedFormatted ago.")
                }

                showDoubleDoseWarningDialog(med, lastLog, elapsedFormatted)
                return
            }
        }

        executeConfirmDose(med, isOverride = false)
    }

    private fun showDoubleDoseWarningDialog(med: Medication, lastLog: DoseLog, elapsedFormatted: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_double_dose_warning, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val formattedLogTime = timeFormat.format(Date(lastLog.takenTimestamp))

        dialogView.findViewById<TextView>(R.id.tvDoubleDoseMedTitle).text =
            "Potential Double-Dose: ${med.name} ${med.dosage}"

        dialogView.findViewById<TextView>(R.id.tvDoubleDoseMessage).text =
            "You already logged taking this medication today at $formattedLogTime ($elapsedFormatted ago)."

        dialogView.findViewById<MaterialButton>(R.id.btnCancelDoubleDose).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Safe choice! Dose logging cancelled.", Toast.LENGTH_SHORT).show()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnConfirmDoubleDose).setOnClickListener {
            dialog.dismiss()
            executeConfirmDose(med, isOverride = true)
        }

        dialog.show()
    }

    private fun executeConfirmDose(med: Medication, isOverride: Boolean) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val log = DoseLog(
            medicationId = med.id,
            medicationName = med.name,
            dosage = med.dosage,
            scheduledTime = timeFormat.format(Date(now)),
            takenTimestamp = now,
            status = "TAKEN",
            notes = if (isOverride) "Intentional safety override" else "Standard dose confirmed"
        )

        dbHelper.logDose(log)
        SoundEffects.playSuccessChime()

        if (profile.voiceEnabled) {
            speechAssistant.speak("Confirmed. ${med.name} ${med.dosage} recorded.")
        }

        Snackbar.make(
            binding.mainCoordinator,
            "✓ Verified Taken: ${med.name} ${med.dosage}",
            Snackbar.LENGTH_LONG
        ).setAction("Undo") {
            refreshMedicationsList()
            updateDashboard()
        }.show()

        refreshMedicationsList()
        updateDashboard()
    }

    private fun showVisualPillVerifyDialog(med: Medication) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_visual_pill_verify, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvVerifyPillTitle)
        val tvDetails = dialogView.findViewById<TextView>(R.id.tvVerifyMedDetails)
        val imgPhoto = dialogView.findViewById<ImageView>(R.id.imgVerifiedPillPhoto)
        val cameraPreview = dialogView.findViewById<PreviewView>(R.id.cameraPreviewView)
        val tvImprint = dialogView.findViewById<TextView>(R.id.tvVerifyImprintOverlay)
        val tvInstructions = dialogView.findViewById<TextView>(R.id.tvVerifyInstructions)
        val btnSnapPhoto = dialogView.findViewById<MaterialButton>(R.id.btnSnapPhoto)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btnConfirmPillMatch)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseVerify)

        tvTitle.text = "Visual Verification • ${med.name}"
        tvDetails.text = "${med.dosage} • ${med.form.uppercase()} shape (${med.condition})"
        tvImprint.text = "Verified Imprint: ${med.imprint.ifBlank { "Unmarked" }}"

        val foodNote = if (med.foodInstruction.isNotBlank()) " | Food guidance: ${med.foodInstruction}" else ""
        tvInstructions.text = "${med.instructions}$foodNote"

        if (!med.photoPath.isNullOrBlank() && File(med.photoPath).exists()) {
            imgPhoto.setImageURI(Uri.fromFile(File(med.photoPath)))
        } else {
            imgPhoto.setImageResource(R.drawable.app_logo)
        }

        var isCameraActive = false

        btnSnapPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                return@setOnClickListener
            }

            if (!isCameraActive) {
                imgPhoto.visibility = View.GONE
                cameraPreview.visibility = View.VISIBLE
                btnSnapPhoto.text = "Capture Photo Now"
                startCameraX(cameraPreview)
                isCameraActive = true
            } else {
                takePhotoForMedication(med) { savedFile ->
                    runOnUiThread {
                        cameraPreview.visibility = View.GONE
                        imgPhoto.visibility = View.VISIBLE
                        imgPhoto.setImageURI(Uri.fromFile(savedFile))
                        btnSnapPhoto.text = "Retake Pill Photo"
                        isCameraActive = false
                        refreshMedicationsList()
                        Toast.makeText(this, "Visual pill photo updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            handleTakeDoseAttempt(med)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startCameraX(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera preview", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoForMedication(med: Medication, onSaved: (File) -> Unit) {
        val capture = imageCapture ?: return
        val photoFile = File(filesDir, "pill_${med.id}_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val updatedMed = med.copy(photoPath = photoFile.absolutePath)
                    dbHelper.updateMedication(updatedMed)
                    onSaved(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Photo capture error: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun exportDoctorPdfReport() {
        try {
            val meds = dbHelper.getAllMedications()
            val adherenceRate = dbHelper.getAdherenceRateLast30Days()
            val pdfFile = PdfReportGenerator.generateDoctorReport(this, profile, meds, adherenceRate)

            val uri = FileProvider.getUriForFile(this, "com.medvault.snappill.fileprovider", pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MedVault Clinical Summary - ${profile.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the doctor-ready 1-page medication and adherence verification summary for ${profile.name}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Doctor Summary PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCaregiverDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_caregiver_link, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvName = dialogView.findViewById<TextView>(R.id.tvCaregiverNameDisplay)
        val tvPhone = dialogView.findViewById<TextView>(R.id.tvCaregiverPhoneDisplay)
        val btnSms = dialogView.findViewById<MaterialButton>(R.id.btnSendCaregiverSms)
        val btnCall = dialogView.findViewById<MaterialButton>(R.id.btnCallCaregiver)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseCaregiver)

        tvName.text = "Caregiver: ${profile.caregiverName}"
        tvPhone.text = "Mobile: ${profile.caregiverPhone}"

        btnSms.setOnClickListener {
            dialog.dismiss()
            val smsText = "MedVault Caregiver Alert: ${profile.name} medication check-in. Morning Lisinopril 10mg logged. Please confirm evening doses."
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${profile.caregiverPhone}")
                putExtra("sms_body", smsText)
            }
            try {
                startActivity(smsIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "No SMS app found. Caregiver ping simulated.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCall.setOnClickListener {
            dialog.dismiss()
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${profile.caregiverPhone}")
            }
            startActivity(callIntent)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showRefillTrackerDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_refill_tracker, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvPharmacyName = dialogView.findViewById<TextView>(R.id.tvPharmacyName)
        val tvPharmacyPhone = dialogView.findViewById<TextView>(R.id.tvPharmacyPhone)
        val btnCallPharmacy = dialogView.findViewById<MaterialButton>(R.id.btnCallPharmacy)
        val btnMarkRefilled = dialogView.findViewById<MaterialButton>(R.id.btnMarkRefilled)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseRefill)

        tvPharmacyName.text = "Pharmacy: ${profile.pharmacy}"
        tvPharmacyPhone.text = "Prescription Line: ${profile.pharmacyPhone}"

        btnCallPharmacy.setOnClickListener {
            dialog.dismiss()
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${profile.pharmacyPhone}")
            }
            startActivity(dialIntent)
        }

        btnMarkRefilled.setOnClickListener {
            val meds = dbHelper.getAllMedications()
            val lowMed = meds.find { it.isLowStock() }
            if (lowMed != null) {
                dbHelper.refillSupply(lowMed.id, 30)
                refreshMedicationsList()
                updateDashboard()
                Toast.makeText(this, "Refill logged (+30 pills for ${lowMed.name})", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showProUpgradeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pro_upgrade, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val btnPurchase = dialogView.findViewById<MaterialButton>(R.id.btnPurchasePro)
        val btnTestToggle = dialogView.findViewById<MaterialButton>(R.id.btnTestUnlockToggle)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClosePro)

        btnPurchase.setOnClickListener {
            profile = profile.copy(isProUser = true)
            dbHelper.saveProfileSetting("isProUser", "true")
            binding.chipProBadge.text = "PRO ACTIVE"
            binding.chipProBadge.setChipBackgroundColorResource(R.color.brand_primary)
            dialog.dismiss()
            SoundEffects.playSuccessChime()
            Toast.makeText(this, "🎉 Welcome to MedVault Pro Lifetime! All features unlocked.", Toast.LENGTH_LONG).show()
        }

        btnTestToggle.setOnClickListener {
            val newState = !profile.isProUser
            profile = profile.copy(isProUser = newState)
            dbHelper.saveProfileSetting("isProUser", newState.toString())
            binding.chipProBadge.text = if (newState) "PRO ACTIVE" else "PRO"
            dialog.dismiss()
            Toast.makeText(this, "Pro status toggled: ${if (newState) "ACTIVE" else "FREE TIER"}", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAddMedicationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medication, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etMedName)
        val etBrand = dialogView.findViewById<TextInputEditText>(R.id.etBrandName)
        val etDosage = dialogView.findViewById<TextInputEditText>(R.id.etDosage)
        val etSchedule = dialogView.findViewById<TextInputEditText>(R.id.etScheduleTime)
        val etSupply = dialogView.findViewById<TextInputEditText>(R.id.etSupply)
        val etCondition = dialogView.findViewById<TextInputEditText>(R.id.etCondition)
        val etInstructions = dialogView.findViewById<TextInputEditText>(R.id.etInstructions)
        val etImprint = dialogView.findViewById<TextInputEditText>(R.id.etImprint)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSaveMed)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancelAddMed)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val dosage = etDosage.text.toString().trim()

            if (name.isBlank() || dosage.isBlank()) {
                Toast.makeText(this, "Medication name & dosage required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newMed = Medication(
                name = name,
                brandName = etBrand.text.toString().trim(),
                dosage = dosage,
                form = "capsule",
                colorHex = "#38BDF8",
                imprint = etImprint.text.toString().trim(),
                scheduledTimes = etSchedule.text.toString().trim().ifBlank { "08:00 AM" },
                condition = etCondition.text.toString().trim(),
                instructions = etInstructions.text.toString().trim(),
                currentSupply = etSupply.text.toString().toIntOrNull() ?: 30,
                refillThreshold = 7,
                rxNumber = "RX-${(100000..999999).random()}"
            )

            dbHelper.insertMedication(newMed)
            dialog.dismiss()
            refreshMedicationsList()
            updateDashboard()
            Toast.makeText(this, "Added ${newMed.name} to schedule", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun checkCameraPermissionAndScanBottle() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            Toast.makeText(this, "Point camera at pill bottle label to scan Rx info", Toast.LENGTH_SHORT).show()
            showAddMedicationDialog()
        }
    }

    private fun toggleSeniorMode() {
        val newSenior = !profile.seniorMode
        profile = profile.copy(seniorMode = newSenior)
        dbHelper.saveProfileSetting("seniorMode", newSenior.toString())

        if (newSenior) {
            binding.tvAppTitle.textSize = 24f
            binding.tvDidITakeItTitle.textSize = 20f
            binding.tvSafetyStatusDetail.textSize = 15f
            Toast.makeText(this, "Senior Mode ON: High-legibility large text & voice enabled", Toast.LENGTH_SHORT).show()
            speechAssistant.speak("Senior Accessibility Mode activated.")
        } else {
            binding.tvAppTitle.textSize = 20f
            binding.tvDidITakeItTitle.textSize = 17f
            binding.tvSafetyStatusDetail.textSize = 13f
            Toast.makeText(this, "Senior Mode OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshMedicationsList() {
        val meds = dbHelper.getAllMedications()
        adapter.updateList(meds)
        binding.tvMedCountBadge.text = "${meds.size} Active Meds"
    }

    private fun updateDashboard() {
        val meds = dbHelper.getAllMedications()
        binding.tvMedCountBadge.text = "${meds.size} Active Meds"

        val lowSupplyCount = meds.count { it.isLowStock() }
        binding.btnRefillAlerts.text = if (lowSupplyCount > 0) "Refills ($lowSupplyCount Alert)" else "Refills (OK)"

        val morningMed = meds.find { it.name.contains("Lisinopril", ignoreCase = true) }
        val eveningMed = meds.find { it.name.contains("Metformin", ignoreCase = true) }

        val statusText = StringBuilder()
        if (morningMed != null) {
            val lastLog = dbHelper.getLastLogForMedication(morningMed.id)
            if (lastLog != null) {
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                statusText.append("Morning ${morningMed.name} ${morningMed.dosage} verified taken at ${timeFormat.format(Date(lastLog.takenTimestamp))}. ")
            }
        }
        if (eveningMed != null) {
            statusText.append("Next: ${eveningMed.name} ${eveningMed.dosage} scheduled for 07:00 PM.")
        } else {
            statusText.append("All scheduled doses up-to-date.")
        }
        binding.tvSafetyStatusDetail.text = statusText.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechAssistant.shutdown()
        cameraExecutor.shutdown()
    }
}
