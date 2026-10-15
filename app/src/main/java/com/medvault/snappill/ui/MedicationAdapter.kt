package com.medvault.snappill.ui

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medvault.snappill.R
import com.medvault.snappill.data.db.MedVaultDbHelper
import com.medvault.snappill.data.model.Medication
import com.medvault.snappill.databinding.ItemMedicationBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedicationAdapter(
    private val context: Context,
    private var medications: List<Medication>,
    private val dbHelper: MedVaultDbHelper,
    private val onTakeDose: (Medication) -> Unit,
    private val onVerifyPill: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.MedViewHolder>() {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

    fun updateList(newList: List<Medication>) {
        medications = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedViewHolder, position: Int) {
        holder.bind(medications[position])
    }

    override fun getItemCount(): Int = medications.size

    inner class MedViewHolder(private val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(med: Medication) {
            binding.tvMedName.text = med.name
            val brandText = if (med.brandName.isNotBlank()) " • ${med.brandName}" else ""
            binding.tvDosageAndBrand.text = "${med.dosage}$brandText"

            val conditionText = if (med.condition.isNotBlank()) "${med.condition} • " else ""
            binding.tvConditionAndSchedule.text = "${conditionText}Daily at ${med.scheduledTimes}"

            val foodNote = if (med.foodInstruction.isNotBlank()) " (${med.foodInstruction})" else ""
            binding.tvInstructions.text = "${med.instructions}$foodNote"

            // Imprint tag
            if (med.imprint.isNotBlank()) {
                binding.tvImprintBadge.visibility = View.VISIBLE
                binding.tvImprintBadge.text = med.imprint
            } else {
                binding.tvImprintBadge.visibility = View.GONE
            }

            // Low Refill Tag
            if (med.isLowStock()) {
                binding.tvRefillTag.visibility = View.VISIBLE
                binding.tvRefillTag.text = "⚠️ ${med.currentSupply} pills left"
            } else {
                binding.tvRefillTag.visibility = View.GONE
            }

            // Custom Pill Photo or Vector Color
            if (!med.photoPath.isNullOrBlank() && File(med.photoPath).exists()) {
                binding.imgPillPhoto.visibility = View.VISIBLE
                binding.imgPillVector.visibility = View.GONE
                binding.imgPillPhoto.setImageURI(Uri.fromFile(File(med.photoPath)))
            } else {
                binding.imgPillPhoto.visibility = View.GONE
                binding.imgPillVector.visibility = View.VISIBLE
                try {
                    val color = Color.parseColor(med.colorHex)
                    binding.imgPillVector.setColorFilter(color)
                } catch (e: Exception) {
                    binding.imgPillVector.setColorFilter(Color.parseColor("#F472B6"))
                }
            }

            // Dose status check for today
            val lastLog = dbHelper.getLastLogForMedication(med.id)
            val isTakenToday = lastLog != null && isSameDay(lastLog.takenTimestamp, System.currentTimeMillis())

            if (isTakenToday && lastLog != null) {
                val formattedTime = timeFormat.format(Date(lastLog.takenTimestamp))
                binding.tvDoseStatus.text = "✓ Taken today at $formattedTime"
                binding.tvDoseStatus.setTextColor(context.getColor(R.color.status_safe))
                binding.btnTakeDose.text = "Take Again"
                binding.btnTakeDose.setBackgroundColor(context.getColor(R.color.bg_card_highlight))
            } else {
                binding.tvDoseStatus.text = "⏰ Scheduled for ${med.scheduledTimes}"
                binding.tvDoseStatus.setTextColor(context.getColor(R.color.brand_primary_light))
                binding.btnTakeDose.text = "Log Taken"
                binding.btnTakeDose.setBackgroundColor(context.getColor(R.color.brand_primary))
            }

            binding.btnTakeDose.setOnClickListener {
                onTakeDose(med)
            }

            binding.btnVerifyPill.setOnClickListener {
                onVerifyPill(med)
            }
        }

        private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
            val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }
}
