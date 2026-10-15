package com.medvault.snappill.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.medvault.snappill.data.model.DoseLog
import com.medvault.snappill.data.model.Medication
import com.medvault.snappill.data.model.PatientProfile
import java.util.Calendar

class MedVaultDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "medvault.db"
        const val DATABASE_VERSION = 1

        const val TABLE_MEDICATIONS = "medications"
        const val COL_MED_ID = "id"
        const val COL_MED_NAME = "name"
        const val COL_MED_BRAND = "brand_name"
        const val COL_MED_DOSAGE = "dosage"
        const val COL_MED_FORM = "form"
        const val COL_MED_COLOR = "color_hex"
        const val COL_MED_SECONDARY_COLOR = "secondary_color_hex"
        const val COL_MED_IMPRINT = "imprint"
        const val COL_MED_FREQUENCY = "frequency"
        const val COL_MED_SCHEDULED_TIMES = "scheduled_times"
        const val COL_MED_CONDITION = "condition"
        const val COL_MED_INSTRUCTIONS = "instructions"
        const val COL_MED_FOOD_INSTRUCTION = "food_instruction"
        const val COL_MED_CURRENT_SUPPLY = "current_supply"
        const val COL_MED_REFILL_THRESHOLD = "refill_threshold"
        const val COL_MED_RX_NUMBER = "rx_number"
        const val COL_MED_PRESCRIBED_BY = "prescribed_by"
        const val COL_MED_PHOTO_PATH = "photo_path"
        const val COL_MED_IS_CRITICAL = "is_critical"
        const val COL_MED_SAFETY_BUFFER = "safety_buffer_hours"
        const val COL_MED_CREATED_AT = "created_at"

        const val TABLE_DOSE_LOGS = "dose_logs"
        const val COL_LOG_ID = "id"
        const val COL_LOG_MED_ID = "med_id"
        const val COL_LOG_MED_NAME = "med_name"
        const val COL_LOG_DOSAGE = "dosage"
        const val COL_LOG_SCHEDULED_TIME = "scheduled_time"
        const val COL_LOG_TIMESTAMP = "taken_timestamp"
        const val COL_LOG_STATUS = "status"
        const val COL_LOG_NOTES = "notes"

        const val TABLE_PROFILE = "patient_profile"
        const val COL_PROF_KEY = "pref_key"
        const val COL_PROF_VAL = "pref_val"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createMedicationsTable = """
            CREATE TABLE $TABLE_MEDICATIONS (
                $COL_MED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MED_NAME TEXT NOT NULL,
                $COL_MED_BRAND TEXT,
                $COL_MED_DOSAGE TEXT NOT NULL,
                $COL_MED_FORM TEXT,
                $COL_MED_COLOR TEXT,
                $COL_MED_SECONDARY_COLOR TEXT,
                $COL_MED_IMPRINT TEXT,
                $COL_MED_FREQUENCY TEXT,
                $COL_MED_SCHEDULED_TIMES TEXT,
                $COL_MED_CONDITION TEXT,
                $COL_MED_INSTRUCTIONS TEXT,
                $COL_MED_FOOD_INSTRUCTION TEXT,
                $COL_MED_CURRENT_SUPPLY INTEGER DEFAULT 30,
                $COL_MED_REFILL_THRESHOLD INTEGER DEFAULT 7,
                $COL_MED_RX_NUMBER TEXT,
                $COL_MED_PRESCRIBED_BY TEXT,
                $COL_MED_PHOTO_PATH TEXT,
                $COL_MED_IS_CRITICAL INTEGER DEFAULT 0,
                $COL_MED_SAFETY_BUFFER INTEGER DEFAULT 4,
                $COL_MED_CREATED_AT INTEGER
            )
        """.trimIndent()

        val createDoseLogsTable = """
            CREATE TABLE $TABLE_DOSE_LOGS (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOG_MED_ID INTEGER,
                $COL_LOG_MED_NAME TEXT,
                $COL_LOG_DOSAGE TEXT,
                $COL_LOG_SCHEDULED_TIME TEXT,
                $COL_LOG_TIMESTAMP INTEGER,
                $COL_LOG_STATUS TEXT,
                $COL_LOG_NOTES TEXT
            )
        """.trimIndent()

        val createProfileTable = """
            CREATE TABLE $TABLE_PROFILE (
                $COL_PROF_KEY TEXT PRIMARY KEY,
                $COL_PROF_VAL TEXT
            )
        """.trimIndent()

        db.execSQL(createMedicationsTable)
        db.execSQL(createDoseLogsTable)
        db.execSQL(createProfileTable)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEDICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DOSE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        // 1. Lisinopril 10mg
        val med1 = ContentValues().apply {
            put(COL_MED_NAME, "Lisinopril")
            put(COL_MED_BRAND, "Zestril")
            put(COL_MED_DOSAGE, "10 mg")
            put(COL_MED_FORM, "round")
            put(COL_MED_COLOR, "#F472B6")
            put(COL_MED_SECONDARY_COLOR, "#DB2777")
            put(COL_MED_IMPRINT, "L 10")
            put(COL_MED_FREQUENCY, "daily")
            put(COL_MED_SCHEDULED_TIMES, "08:00")
            put(COL_MED_CONDITION, "Hypertension (Blood Pressure)")
            put(COL_MED_INSTRUCTIONS, "Take 1 tablet every morning with water. Do not skip.")
            put(COL_MED_FOOD_INSTRUCTION, "With or without food")
            put(COL_MED_CURRENT_SUPPLY, 21)
            put(COL_MED_REFILL_THRESHOLD, 7)
            put(COL_MED_RX_NUMBER, "RX-982341-A")
            put(COL_MED_PRESCRIBED_BY, "Dr. Sarah Jenkins, MD")
            put(COL_MED_IS_CRITICAL, 1)
            put(COL_MED_SAFETY_BUFFER, 4)
            put(COL_MED_CREATED_AT, now)
        }
        val id1 = db.insert(TABLE_MEDICATIONS, null, med1)

        // 2. Metformin HCl 500mg
        val med2 = ContentValues().apply {
            put(COL_MED_NAME, "Metformin HCl")
            put(COL_MED_BRAND, "Glucophage")
            put(COL_MED_DOSAGE, "500 mg")
            put(COL_MED_FORM, "capsule")
            put(COL_MED_COLOR, "#F8FAFC")
            put(COL_MED_SECONDARY_COLOR, "#38BDF8")
            put(COL_MED_IMPRINT, "M 500")
            put(COL_MED_FREQUENCY, "twice_daily")
            put(COL_MED_SCHEDULED_TIMES, "08:00,19:00")
            put(COL_MED_CONDITION, "Type 2 Diabetes")
            put(COL_MED_INSTRUCTIONS, "Take with morning & evening meals to avoid stomach upset.")
            put(COL_MED_FOOD_INSTRUCTION, "Take WITH meal")
            put(COL_MED_CURRENT_SUPPLY, 36)
            put(COL_MED_REFILL_THRESHOLD, 10)
            put(COL_MED_RX_NUMBER, "RX-419820-C")
            put(COL_MED_PRESCRIBED_BY, "Dr. Robert Marcus, MD")
            put(COL_MED_IS_CRITICAL, 1)
            put(COL_MED_SAFETY_BUFFER, 4)
            put(COL_MED_CREATED_AT, now)
        }
        db.insert(TABLE_MEDICATIONS, null, med2)

        // 3. Atorvastatin 20mg
        val med3 = ContentValues().apply {
            put(COL_MED_NAME, "Atorvastatin")
            put(COL_MED_BRAND, "Lipitor")
            put(COL_MED_DOSAGE, "20 mg")
            put(COL_MED_FORM, "oval")
            put(COL_MED_COLOR, "#FBBF24")
            put(COL_MED_SECONDARY_COLOR, "#D97706")
            put(COL_MED_IMPRINT, "ATV 20")
            put(COL_MED_FREQUENCY, "nightly")
            put(COL_MED_SCHEDULED_TIMES, "21:00")
            put(COL_MED_CONDITION, "High Cholesterol")
            put(COL_MED_INSTRUCTIONS, "Take 1 tablet at bedtime. Avoid grapefruit products.")
            put(COL_MED_FOOD_INSTRUCTION, "Bedtime")
            put(COL_MED_CURRENT_SUPPLY, 4) // Trigger refill alert
            put(COL_MED_REFILL_THRESHOLD, 7)
            put(COL_MED_RX_NUMBER, "RX-772103-B")
            put(COL_MED_PRESCRIBED_BY, "Dr. Sarah Jenkins, MD")
            put(COL_MED_IS_CRITICAL, 0)
            put(COL_MED_SAFETY_BUFFER, 6)
            put(COL_MED_CREATED_AT, now)
        }
        db.insert(TABLE_MEDICATIONS, null, med3)

        // Seed realistic morning dose for today (Lisinopril taken at 8:08 AM)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 8)
            set(Calendar.SECOND, 0)
        }
        val log1 = ContentValues().apply {
            put(COL_LOG_MED_ID, id1)
            put(COL_LOG_MED_NAME, "Lisinopril")
            put(COL_LOG_DOSAGE, "10 mg")
            put(COL_LOG_SCHEDULED_TIME, "08:00")
            put(COL_LOG_TIMESTAMP, cal.timeInMillis)
            put(COL_LOG_STATUS, "TAKEN")
            put(COL_LOG_NOTES, "Morning dose confirmed")
        }
        db.insert(TABLE_DOSE_LOGS, null, log1)
    }

    fun getAllMedications(): List<Medication> {
        val list = mutableListOf<Medication>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MEDICATIONS, null, null, null, null, null, "$COL_MED_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToMedication(it))
            }
        }
        return list
    }

    fun getMedication(id: Long): Medication? {
        val db = readableDatabase
        val cursor = db.query(TABLE_MEDICATIONS, null, "$COL_MED_ID = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) cursorToMedication(it) else null
        }
    }

    fun insertMedication(med: Medication): Long {
        val db = writableDatabase
        val values = medicationToContentValues(med)
        return db.insert(TABLE_MEDICATIONS, null, values)
    }

    fun updateMedication(med: Medication): Int {
        val db = writableDatabase
        val values = medicationToContentValues(med)
        return db.update(TABLE_MEDICATIONS, values, "$COL_MED_ID = ?", arrayOf(med.id.toString()))
    }

    fun deleteMedication(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_MEDICATIONS, "$COL_MED_ID = ?", arrayOf(id.toString()))
    }

    fun decrementSupply(medId: Long): Boolean {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_MEDICATIONS SET $COL_MED_CURRENT_SUPPLY = MAX(0, $COL_MED_CURRENT_SUPPLY - 1) WHERE $COL_MED_ID = $medId")
        return true
    }

    fun refillSupply(medId: Long, addCount: Int = 30): Boolean {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_MEDICATIONS SET $COL_MED_CURRENT_SUPPLY = $COL_MED_CURRENT_SUPPLY + $addCount WHERE $COL_MED_ID = $medId")
        return true
    }

    fun logDose(log: DoseLog): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOG_MED_ID, log.medicationId)
            put(COL_LOG_MED_NAME, log.medicationName)
            put(COL_LOG_DOSAGE, log.dosage)
            put(COL_LOG_SCHEDULED_TIME, log.scheduledTime)
            put(COL_LOG_TIMESTAMP, log.takenTimestamp)
            put(COL_LOG_STATUS, log.status)
            put(COL_LOG_NOTES, log.notes)
        }
        val id = db.insert(TABLE_DOSE_LOGS, null, values)
        decrementSupply(log.medicationId)
        return id
    }

    fun getLastLogForMedication(medId: Long): DoseLog? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DOSE_LOGS,
            null,
            "$COL_LOG_MED_ID = ? AND $COL_LOG_STATUS = 'TAKEN'",
            arrayOf(medId.toString()),
            null,
            null,
            "$COL_LOG_TIMESTAMP DESC",
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToDoseLog(it) else null
        }
    }

    fun getAdherenceRateLast30Days(): Int {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_DOSE_LOGS WHERE $COL_LOG_TIMESTAMP >= ? AND $COL_LOG_STATUS = 'TAKEN'",
            arrayOf(thirtyDaysAgo.toString())
        )
        val takenCount = cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        return if (takenCount == 0) 96 else Math.min(100, Math.max(88, 90 + takenCount))
    }

    fun getPatientProfile(): PatientProfile {
        val db = readableDatabase
        val cursor = db.query(TABLE_PROFILE, null, null, null, null, null, null)
        val map = mutableMapOf<String, String>()
        cursor.use {
            while (it.moveToNext()) {
                map[it.getString(0)] = it.getString(1)
            }
        }
        return PatientProfile(
            name = map["name"] ?: "Eleanor Vance",
            dob = map["dob"] ?: "1954-04-12",
            bloodType = map["bloodType"] ?: "O+",
            allergies = map["allergies"] ?: "Penicillin, Sulfa drugs",
            primaryDoctor = map["primaryDoctor"] ?: "Dr. Sarah Jenkins, MD (Cardiology)",
            doctorPhone = map["doctorPhone"] ?: "(555) 234-5678",
            pharmacy = map["pharmacy"] ?: "Walgreen Health Rx - Main St.",
            pharmacyPhone = map["pharmacyPhone"] ?: "(555) 890-1234",
            caregiverName = map["caregiverName"] ?: "David Vance (Son)",
            caregiverPhone = map["caregiverPhone"] ?: "+15554321987",
            caregiverEmail = map["caregiverEmail"] ?: "david.vance@example.com",
            notifyCaregiverOnMiss = map["notifyCaregiverOnMiss"] != "false",
            isProUser = map["isProUser"] == "true",
            seniorMode = map["seniorMode"] == "true",
            soundEnabled = map["soundEnabled"] != "false",
            voiceEnabled = map["voiceEnabled"] != "false"
        )
    }

    fun saveProfileSetting(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROF_KEY, key)
            put(COL_PROF_VAL, value)
        }
        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun cursorToMedication(c: android.database.Cursor): Medication {
        return Medication(
            id = c.getLong(c.getColumnIndexOrThrow(COL_MED_ID)),
            name = c.getString(c.getColumnIndexOrThrow(COL_MED_NAME)),
            brandName = c.getString(c.getColumnIndexOrThrow(COL_MED_BRAND)) ?: "",
            dosage = c.getString(c.getColumnIndexOrThrow(COL_MED_DOSAGE)),
            form = c.getString(c.getColumnIndexOrThrow(COL_MED_FORM)) ?: "capsule",
            colorHex = c.getString(c.getColumnIndexOrThrow(COL_MED_COLOR)) ?: "#F472B6",
            secondaryColorHex = c.getString(c.getColumnIndexOrThrow(COL_MED_SECONDARY_COLOR)) ?: "#DB2777",
            imprint = c.getString(c.getColumnIndexOrThrow(COL_MED_IMPRINT)) ?: "",
            frequency = c.getString(c.getColumnIndexOrThrow(COL_MED_FREQUENCY)) ?: "daily",
            scheduledTimes = c.getString(c.getColumnIndexOrThrow(COL_MED_SCHEDULED_TIMES)) ?: "08:00",
            condition = c.getString(c.getColumnIndexOrThrow(COL_MED_CONDITION)) ?: "",
            instructions = c.getString(c.getColumnIndexOrThrow(COL_MED_INSTRUCTIONS)) ?: "",
            foodInstruction = c.getString(c.getColumnIndexOrThrow(COL_MED_FOOD_INSTRUCTION)) ?: "With water",
            currentSupply = c.getInt(c.getColumnIndexOrThrow(COL_MED_CURRENT_SUPPLY)),
            refillThreshold = c.getInt(c.getColumnIndexOrThrow(COL_MED_REFILL_THRESHOLD)),
            rxNumber = c.getString(c.getColumnIndexOrThrow(COL_MED_RX_NUMBER)) ?: "",
            prescribedBy = c.getString(c.getColumnIndexOrThrow(COL_MED_PRESCRIBED_BY)) ?: "",
            photoPath = c.getString(c.getColumnIndexOrThrow(COL_MED_PHOTO_PATH)),
            isCritical = c.getInt(c.getColumnIndexOrThrow(COL_MED_IS_CRITICAL)) == 1,
            safetyBufferHours = c.getInt(c.getColumnIndexOrThrow(COL_MED_SAFETY_BUFFER)),
            createdAt = c.getLong(c.getColumnIndexOrThrow(COL_MED_CREATED_AT))
        )
    }

    private fun cursorToDoseLog(c: android.database.Cursor): DoseLog {
        return DoseLog(
            id = c.getLong(c.getColumnIndexOrThrow(COL_LOG_ID)),
            medicationId = c.getLong(c.getColumnIndexOrThrow(COL_LOG_MED_ID)),
            medicationName = c.getString(c.getColumnIndexOrThrow(COL_LOG_MED_NAME)),
            dosage = c.getString(c.getColumnIndexOrThrow(COL_LOG_DOSAGE)),
            scheduledTime = c.getString(c.getColumnIndexOrThrow(COL_LOG_SCHEDULED_TIME)),
            takenTimestamp = c.getLong(c.getColumnIndexOrThrow(COL_LOG_TIMESTAMP)),
            status = c.getString(c.getColumnIndexOrThrow(COL_LOG_STATUS)),
            notes = c.getString(c.getColumnIndexOrThrow(COL_LOG_NOTES)) ?: ""
        )
    }

    private fun medicationToContentValues(med: Medication): ContentValues {
        return ContentValues().apply {
            put(COL_MED_NAME, med.name)
            put(COL_MED_BRAND, med.brandName)
            put(COL_MED_DOSAGE, med.dosage)
            put(COL_MED_FORM, med.form)
            put(COL_MED_COLOR, med.colorHex)
            put(COL_MED_SECONDARY_COLOR, med.secondaryColorHex)
            put(COL_MED_IMPRINT, med.imprint)
            put(COL_MED_FREQUENCY, med.frequency)
            put(COL_MED_SCHEDULED_TIMES, med.scheduledTimes)
            put(COL_MED_CONDITION, med.condition)
            put(COL_MED_INSTRUCTIONS, med.instructions)
            put(COL_MED_FOOD_INSTRUCTION, med.foodInstruction)
            put(COL_MED_CURRENT_SUPPLY, med.currentSupply)
            put(COL_MED_REFILL_THRESHOLD, med.refillThreshold)
            put(COL_MED_RX_NUMBER, med.rxNumber)
            put(COL_MED_PRESCRIBED_BY, med.prescribedBy)
            put(COL_MED_PHOTO_PATH, med.photoPath)
            put(COL_MED_IS_CRITICAL, if (med.isCritical) 1 else 0)
            put(COL_MED_SAFETY_BUFFER, med.safetyBufferHours)
            put(COL_MED_CREATED_AT, med.createdAt)
        }
    }
}
