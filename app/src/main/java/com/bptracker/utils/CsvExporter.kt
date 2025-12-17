package com.bptracker.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bptracker.data.model.BloodPressureReading
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CsvExporter {
    
    fun exportReadings(context: Context, readings: List<BloodPressureReading>) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "bp_readings_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.csv"
            val file = File(exportDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.appendLine("Date,Time,Systolic (mmHg),Diastolic (mmHg),Pulse (bpm),MAP (mmHg),Pulse Pressure (mmHg),Category,Arm,Position,Tag,Mood,Stress,Notes")
                
                readings.forEach { reading ->
                    val date = reading.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = reading.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val moodLabel = getMoodLabel(reading.mood)
                    val stressLabel = getStressLabel(reading.stressLevel)
                    val notes = reading.notes.replace(",", ";").replace("\n", " ")
                    
                    writer.appendLine(
                        "$date,$time,${reading.systolic},${reading.diastolic},${reading.pulse}," +
                        "${reading.formattedMAP},${reading.pulsePressure},${reading.category.label}," +
                        "${reading.armPosition.label},${reading.bodyPosition.label},${reading.tag.label}," +
                        "$moodLabel,$stressLabel,\"$notes\""
                    )
                }
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share CSV Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun getMoodLabel(mood: Int): String = when (mood) {
        5 -> "Very Happy"
        4 -> "Happy"
        3 -> "Neutral"
        2 -> "Stressed"
        1 -> "Very Stressed"
        else -> "Unknown"
    }
    
    private fun getStressLabel(stress: Int): String = when (stress) {
        1 -> "None"
        2 -> "Low"
        3 -> "Moderate"
        4 -> "High"
        5 -> "Severe"
        else -> "Unknown"
    }
}
