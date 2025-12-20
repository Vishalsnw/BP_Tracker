package com.vitalflowapp.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vitalflowapp.data.model.BloodPressureReading
import com.vitalflowapp.data.model.Medication
import com.vitalflowapp.data.model.UserProfile
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DoctorReportGenerator {
    
    fun generateDoctorVisitReport(
        context: Context,
        profile: UserProfile?,
        readings: List<BloodPressureReading>,
        medications: List<Medication>,
        periodDays: Int = 30
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "doctor_visit_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"
            val file = File(exportDir, fileName)
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            
            document.add(
                Paragraph("Blood Pressure Summary Report")
                    .setFontSize(22f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(
                Paragraph("Prepared for Doctor Visit")
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5f)
            )
            
            document.add(
                Paragraph("Report Date: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            if (profile != null) {
                document.add(
                    Paragraph("Patient Information")
                        .setFontSize(14f)
                        .setBold()
                        .setMarginTop(10f)
                )
                document.add(Paragraph("Name: ${profile.name}").setFontSize(11f))
                if (profile.dateOfBirth != null) {
                    document.add(Paragraph("Date of Birth: ${profile.dateOfBirth}").setFontSize(11f))
                }
                document.add(Paragraph("Gender: ${profile.gender.label}").setFontSize(11f).setMarginBottom(15f))
            }
            
            val cutoffDate = LocalDateTime.now().minusDays(periodDays.toLong())
            val periodReadings = readings.filter { it.timestamp.isAfter(cutoffDate) }
            
            document.add(
                Paragraph("Summary Statistics (Last $periodDays Days)")
                    .setFontSize(14f)
                    .setBold()
                    .setMarginTop(10f)
            )
            
            if (periodReadings.isNotEmpty()) {
                val avgSystolic = periodReadings.map { it.systolic }.average()
                val avgDiastolic = periodReadings.map { it.diastolic }.average()
                val avgPulse = periodReadings.map { it.pulse }.average()
                val avgMAP = periodReadings.map { it.meanArterialPressure }.average()
                val avgPulsePressure = periodReadings.map { it.pulsePressure }.average()
                
                val minSystolic = periodReadings.minOf { it.systolic }
                val maxSystolic = periodReadings.maxOf { it.systolic }
                val minDiastolic = periodReadings.minOf { it.diastolic }
                val maxDiastolic = periodReadings.maxOf { it.diastolic }
                
                val morningReadings = periodReadings.filter { it.timestamp.hour < 12 }
                val eveningReadings = periodReadings.filter { it.timestamp.hour >= 17 }
                
                val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 30f, 30f)))
                    .useAllAvailableWidth()
                
                listOf("Metric", "Value", "Range").forEach { header ->
                    statsTable.addHeaderCell(
                        Cell().add(Paragraph(header).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                }
                
                statsTable.addCell(Cell().add(Paragraph("Total Readings")))
                statsTable.addCell(Cell().add(Paragraph("${periodReadings.size}").setTextAlignment(TextAlignment.CENTER)))
                statsTable.addCell(Cell().add(Paragraph("-")))
                
                statsTable.addCell(Cell().add(Paragraph("Average Blood Pressure")))
                statsTable.addCell(Cell().add(Paragraph("%.0f/%.0f mmHg".format(avgSystolic, avgDiastolic)).setTextAlignment(TextAlignment.CENTER)))
                statsTable.addCell(Cell().add(Paragraph("$minSystolic-$maxSystolic / $minDiastolic-$maxDiastolic")))
                
                statsTable.addCell(Cell().add(Paragraph("Average Pulse")))
                statsTable.addCell(Cell().add(Paragraph("%.0f bpm".format(avgPulse)).setTextAlignment(TextAlignment.CENTER)))
                statsTable.addCell(Cell().add(Paragraph("-")))
                
                statsTable.addCell(Cell().add(Paragraph("Mean Arterial Pressure (MAP)")))
                statsTable.addCell(Cell().add(Paragraph("%.1f mmHg".format(avgMAP)).setTextAlignment(TextAlignment.CENTER)))
                statsTable.addCell(Cell().add(Paragraph("-")))
                
                statsTable.addCell(Cell().add(Paragraph("Average Pulse Pressure")))
                statsTable.addCell(Cell().add(Paragraph("%.0f mmHg".format(avgPulsePressure)).setTextAlignment(TextAlignment.CENTER)))
                statsTable.addCell(Cell().add(Paragraph("-")))
                
                if (morningReadings.isNotEmpty()) {
                    val morningAvgSys = morningReadings.map { it.systolic }.average()
                    val morningAvgDia = morningReadings.map { it.diastolic }.average()
                    statsTable.addCell(Cell().add(Paragraph("Morning Average (before noon)")))
                    statsTable.addCell(Cell().add(Paragraph("%.0f/%.0f mmHg".format(morningAvgSys, morningAvgDia)).setTextAlignment(TextAlignment.CENTER)))
                    statsTable.addCell(Cell().add(Paragraph("${morningReadings.size} readings")))
                }
                
                if (eveningReadings.isNotEmpty()) {
                    val eveningAvgSys = eveningReadings.map { it.systolic }.average()
                    val eveningAvgDia = eveningReadings.map { it.diastolic }.average()
                    statsTable.addCell(Cell().add(Paragraph("Evening Average (after 5pm)")))
                    statsTable.addCell(Cell().add(Paragraph("%.0f/%.0f mmHg".format(eveningAvgSys, eveningAvgDia)).setTextAlignment(TextAlignment.CENTER)))
                    statsTable.addCell(Cell().add(Paragraph("${eveningReadings.size} readings")))
                }
                
                document.add(statsTable)
                
                val categoryBreakdown = periodReadings.groupBy { it.category }
                document.add(
                    Paragraph("Category Distribution")
                        .setFontSize(12f)
                        .setBold()
                        .setMarginTop(15f)
                )
                categoryBreakdown.forEach { (category, list) ->
                    val percentage = (list.size.toDouble() / periodReadings.size * 100).toInt()
                    document.add(Paragraph("${category.label}: ${list.size} readings ($percentage%)").setFontSize(10f))
                }
            } else {
                document.add(Paragraph("No readings recorded in the last $periodDays days.").setFontSize(11f))
            }
            
            if (medications.isNotEmpty()) {
                document.add(
                    Paragraph("Current Medications")
                        .setFontSize(14f)
                        .setBold()
                        .setMarginTop(20f)
                )
                
                val medTable = Table(UnitValue.createPercentArray(floatArrayOf(35f, 25f, 40f)))
                    .useAllAvailableWidth()
                
                listOf("Medication", "Dosage", "Frequency").forEach { header ->
                    medTable.addHeaderCell(
                        Cell().add(Paragraph(header).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                }
                
                medications.filter { it.isActive }.forEach { med ->
                    medTable.addCell(Cell().add(Paragraph(med.name)))
                    medTable.addCell(Cell().add(Paragraph(med.dosage).setTextAlignment(TextAlignment.CENTER)))
                    medTable.addCell(Cell().add(Paragraph(med.frequency.label)))
                }
                
                document.add(medTable)
            }
            
            if (periodReadings.size >= 7) {
                document.add(
                    Paragraph("Trend Analysis")
                        .setFontSize(14f)
                        .setBold()
                        .setMarginTop(20f)
                )
                
                val firstWeekReadings = periodReadings.takeLast(periodReadings.size / 2)
                val lastWeekReadings = periodReadings.take(periodReadings.size / 2)
                
                if (firstWeekReadings.isNotEmpty() && lastWeekReadings.isNotEmpty()) {
                    val firstAvgSys = firstWeekReadings.map { it.systolic }.average()
                    val lastAvgSys = lastWeekReadings.map { it.systolic }.average()
                    val trend = lastAvgSys - firstAvgSys
                    
                    val trendText = when {
                        trend > 5 -> "Systolic BP has increased by %.0f mmHg on average.".format(trend)
                        trend < -5 -> "Systolic BP has decreased by %.0f mmHg on average.".format(-trend)
                        else -> "Blood pressure has remained relatively stable."
                    }
                    document.add(Paragraph(trendText).setFontSize(11f))
                }
            }
            
            document.add(
                Paragraph("\nDisclaimer: This report is generated for informational purposes to assist in medical consultations. It should not be used as a substitute for professional medical diagnosis or treatment.")
                    .setFontSize(8f)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30f)
            )
            
            document.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Blood Pressure Report for Doctor Visit")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Doctor Visit Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
