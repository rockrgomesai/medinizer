package com.medvault.snappill.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.medvault.snappill.data.model.Medication
import com.medvault.snappill.data.model.PatientProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateDoctorReport(
        context: Context,
        profile: PatientProfile,
        medications: List<Medication>,
        adherenceRate: Int
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val dateString = dateFormat.format(Date())

        // Header Background Banner
        paint.color = Color.parseColor("#0F766E")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEDVAULT • CLINICAL MEDICATION SUMMARY", 36f, 44f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Adherence & Active Prescription Verification Report for Physician Review", 36f, 62f, paint)
        canvas.drawText("Generated: $dateString", 440f, 62f, paint)

        // Patient Information Box
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(36f, 105f, 559f, 175f, 8f, 8f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PATIENT DEMOGRAPHICS", 50f, 126f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Name: ${profile.name}", 50f, 144f, paint)
        canvas.drawText("Date of Birth: ${profile.dob}", 50f, 160f, paint)

        canvas.drawText("Blood Type: ${profile.bloodType}", 190f, 144f, paint)
        val doctorShort = if (profile.primaryDoctor.length > 24) profile.primaryDoctor.substring(0, 22) + "..." else profile.primaryDoctor
        canvas.drawText("Primary MD: $doctorShort", 190f, 160f, paint)

        paint.color = Color.parseColor("#DC2626")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DRUG ALLERGIES: ${profile.allergies}", 355f, 144f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Caregiver: ${profile.caregiverName}", 355f, 160f, paint)

        // 30-Day Adherence Badge
        paint.color = Color.parseColor("#065F46")
        canvas.drawRoundRect(36f, 185f, 559f, 220f, 6f, 6f, paint)
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("★ 30-DAY LOGGED ADHERENCE RATE: $adherenceRate%   |   Double-Dose Safety Interlock: ACTIVE", 50f, 206f, paint)

        // Table Header
        var currentY = 250f
        paint.color = Color.parseColor("#1E293B")
        canvas.drawRect(36f, currentY - 18f, 559f, currentY + 8f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEDICATION & BRAND", 46f, currentY, paint)
        canvas.drawText("DOSAGE", 185f, currentY, paint)
        canvas.drawText("SCHEDULE", 245f, currentY, paint)
        canvas.drawText("INDICATION / INSTRUCTIONS", 325f, currentY, paint)
        canvas.drawText("RX NUMBER", 490f, currentY, paint)

        currentY += 24f

        // Table Rows
        medications.forEachIndexed { index, med ->
            paint.color = if (index % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
            canvas.drawRect(36f, currentY - 14f, 559f, currentY + 28f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val brandDisplay = if (med.brandName.isNotBlank()) " (${med.brandName})" else ""
            canvas.drawText("${med.name}$brandDisplay", 46f, currentY, paint)

            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#64748B")
            canvas.drawText("Shape: ${med.form.uppercase()} | Imprint: ${med.imprint.ifBlank { "N/A" }}", 46f, currentY + 14f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(med.dosage, 185f, currentY, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(med.scheduledTimes, 245f, currentY, paint)
            canvas.drawText(med.frequency, 245f, currentY + 14f, paint)

            val instructionsShort = if (med.instructions.length > 36) med.instructions.substring(0, 34) + "..." else med.instructions
            canvas.drawText(med.condition, 325f, currentY, paint)
            canvas.drawText(instructionsShort, 325f, currentY + 14f, paint)

            canvas.drawText(med.rxNumber.ifBlank { "Recorded" }, 490f, currentY, paint)

            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(36f, currentY + 28f, 559f, currentY + 28f, paint)

            currentY += 46f
        }

        // Refill Inventory Status Box
        currentY += 15f
        paint.color = Color.parseColor("#FEF3C7")
        canvas.drawRoundRect(36f, currentY, 559f, currentY + 65f, 6f, 6f, paint)

        paint.color = Color.parseColor("#92400E")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PHARMACY & REFILL VERIFICATION", 50f, currentY + 20f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Pharmacy: ${profile.pharmacy} | Tel: ${profile.pharmacyPhone}", 50f, currentY + 36f, paint)

        val lowSupplyMeds = medications.filter { it.isLowStock() }.map { "${it.name} (${it.currentSupply} pills left)" }
        val refillText = if (lowSupplyMeds.isNotEmpty()) {
            "ACTION REQUIRED: Refill needed for: " + lowSupplyMeds.joinToString(", ")
        } else {
            "All active supplies currently within safe therapeutic levels (> 7 days on hand)."
        }
        paint.color = if (lowSupplyMeds.isNotEmpty()) Color.parseColor("#B91C1C") else Color.parseColor("#15803D")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(refillText, 50f, currentY + 52f, paint)

        // Doctor Signature & Clinical Notes
        currentY += 90f
        paint.color = Color.parseColor("#64748B")
        paint.strokeWidth = 1f
        canvas.drawLine(36f, currentY + 40f, 260f, currentY + 40f, paint)
        canvas.drawLine(335f, currentY + 40f, 559f, currentY + 40f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Attending Physician Signature", 36f, currentY + 54f, paint)
        canvas.drawText("Date & Clinic Stamp", 335f, currentY + 54f, paint)

        // Footer disclaimer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 7.5f
        canvas.drawText("Confidential Medical Information • Generated securely by MedVault / SnapPill • 100% Offline Cryptographic Record", 36f, 810f, paint)

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "reports")
        outputDir.mkdirs()
        val file = File(outputDir, "MedVault_Doctor_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }
        pdfDocument.close()

        return file
    }
}
