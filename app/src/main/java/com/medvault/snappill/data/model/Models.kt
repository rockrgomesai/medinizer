package com.medvault.snappill.data.model

import java.io.Serializable

data class Medication(
    val id: Long = 0,
    val name: String,
    val brandName: String = "",
    val dosage: String,
    val form: String = "capsule", // round, capsule, oval, square, drop, inhaler, injection
    val colorHex: String = "#F472B6",
    val secondaryColorHex: String = "#DB2777",
    val imprint: String = "",
    val frequency: String = "daily",
    val scheduledTimes: String = "08:00", // comma-separated HH:mm
    val condition: String = "",
    val instructions: String = "",
    val foodInstruction: String = "With water",
    val currentSupply: Int = 30,
    val refillThreshold: Int = 7,
    val rxNumber: String = "",
    val prescribedBy: String = "",
    val photoPath: String? = null,
    val isCritical: Boolean = false,
    val safetyBufferHours: Int = 4, // Prevents accidental double dosing within this time
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {
    fun isLowStock(): Boolean = currentSupply <= refillThreshold
}

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledTime: String,
    val takenTimestamp: Long = System.currentTimeMillis(),
    val status: String = "TAKEN", // TAKEN, SKIPPED, SNOOZED
    val notes: String = ""
) : Serializable

data class PatientProfile(
    val name: String = "Eleanor Vance",
    val dob: String = "1954-04-12",
    val bloodType: String = "O+",
    val allergies: String = "Penicillin, Sulfa drugs",
    val primaryDoctor: String = "Dr. Sarah Jenkins, MD (Cardiology)",
    val doctorPhone: String = "(555) 234-5678",
    val pharmacy: String = "Walgreen Health Rx - Main St.",
    val pharmacyPhone: String = "(555) 890-1234",
    val caregiverName: String = "David Vance (Son)",
    val caregiverPhone: String = "+15554321987",
    val caregiverEmail: String = "david.vance@example.com",
    val notifyCaregiverOnMiss: Boolean = true,
    val isProUser: Boolean = false,
    val seniorMode: Boolean = false,
    val soundEnabled: Boolean = true,
    val voiceEnabled: Boolean = true
) : Serializable
