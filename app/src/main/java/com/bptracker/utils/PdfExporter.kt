package com.bptracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.Medication
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
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

object PdfExporter {
    
    private val PRIMARY_COLOR = DeviceRgb(25, 118, 210)
    private val HEADER_BG = DeviceRgb(227, 242, 253)
    private val NORMAL_COLOR = DeviceRgb(76, 175, 80)
    private val ELEVATED_COLOR = DeviceRgb(255, 193, 7)
    private val HIGH_COLOR = DeviceRgb(255, 152, 0)
    private val CRISIS_COLOR = DeviceRgb(244, 67, 54)
    
    fun exportReadings(context: Context, readings: List<BloodPressureReading>) {
        exportEnhancedReport(context, readings, emptyList(), null)
    }
    
    fun exportEnhancedReport(
        context: Context,
        readings: List<BloodPressureReading>,
        medications: List<Medication> = emptyList(),
        patientName: String? = null
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "bp_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
            val file = File(exportDir, fileName)
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            
            addCoverPage(document, readings, patientName)
            
            if (readings.isNotEmpty()) {
                addStatisticsSection(document, readings)
                addCategoryDistribution(document, readings)
                addTrendAnalysis(document, readings)
            }
            
            if (medications.isNotEmpty()) {
                addMedicationSection(document, medications)
            }
            
            addReadingHistory(document, readings)
            addDisclaimer(document)
            
            document.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    fun exportAndEmailToDoctor(
        context: Context,
        readings: List<BloodPressureReading>,
        medications: List<Medication> = emptyList(),
        patientName: String?,
        doctorEmail: String?
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val fileName = "bp_doctor_report_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
            val file = File(exportDir, fileName)
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)
            
            addCoverPage(document, readings, patientName)
            
            if (readings.isNotEmpty()) {
                addStatisticsSection(document, readings)
                addCategoryDistribution(document, readings)
                addTrendAnalysis(document, readings)
            }
            
            if (medications.isNotEmpty()) {
                addMedicationSection(document, medications)
            }
            
            addReadingHistory(document, readings)
            addDisclaimer(document)
            
            document.close()
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val summary = generateEmailSummary(readings, patientName)
            
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, if (doctorEmail != null) arrayOf(doctorEmail) else arrayOf<String>())
                putExtra(Intent.EXTRA_SUBJECT, "Blood Pressure Report - ${patientName ?: "Patient"}")
                putExtra(Intent.EXTRA_TEXT, summary)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(emailIntent, "Send Report to Doctor").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to send report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun generateEmailSummary(readings: List<BloodPressureReading>, patientName: String?): String {
        val sb = StringBuilder()
        sb.appendLine("Dear Doctor,")
        sb.appendLine()
        sb.appendLine("Please find attached my blood pressure report.")
        sb.appendLine()
        
        if (readings.isNotEmpty()) {
            val avgSystolic = readings.map { it.systolic }.average()
            val avgDiastolic = readings.map { it.diastolic }.average()
            val avgPulse = readings.map { it.pulse }.average()
            val dateRange = "${readings.minByOrNull { it.timestamp }?.formattedDate} - ${readings.maxByOrNull { it.timestamp }?.formattedDate}"
            
            sb.appendLine("Summary:")
            sb.appendLine("- Report Period: $dateRange")
            sb.appendLine("- Total Readings: ${readings.size}")
            sb.appendLine("- Average BP: ${String.format("%.0f", avgSystolic)}/${String.format("%.0f", avgDiastolic)} mmHg")
            sb.appendLine("- Average Pulse: ${String.format("%.0f", avgPulse)} bpm")
            sb.appendLine()
            
            val categories = readings.groupBy { it.category }
            sb.appendLine("Category Distribution:")
            categories.forEach { (category, list) ->
                val percentage = (list.size.toFloat() / readings.size * 100)
                sb.appendLine("- ${category.label}: ${list.size} (${String.format("%.1f", percentage)}%)")
            }
        }
        
        sb.appendLine()
        sb.appendLine("Best regards,")
        sb.appendLine(patientName ?: "Patient")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("This report was generated by Blood Pressure Tracker app.")
        
        return sb.toString()
    }
    
    private fun addCoverPage(document: Document, readings: List<BloodPressureReading>, patientName: String?) {
        document.add(
            Paragraph("Blood Pressure Report")
                .setFontSize(28f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(40f)
        )
        
        if (patientName != null) {
            document.add(
                Paragraph("Patient: $patientName")
                    .setFontSize(14f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10f)
            )
        }
        
        document.add(
            Paragraph("Generated on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))}")
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30f)
        )
        
        if (readings.isNotEmpty()) {
            val dateRange = "${readings.minByOrNull { it.timestamp }?.formattedDate} - ${readings.maxByOrNull { it.timestamp }?.formattedDate}"
            document.add(
                Paragraph("Report Period: $dateRange")
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
        }
    }
    
    private fun addStatisticsSection(document: Document, readings: List<BloodPressureReading>) {
        document.add(
            Paragraph("Statistics Summary")
                .setFontSize(18f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20f)
                .setMarginBottom(10f)
        )
        
        val avgSystolic = readings.map { it.systolic }.average()
        val avgDiastolic = readings.map { it.diastolic }.average()
        val avgPulse = readings.map { it.pulse }.average()
        val minSystolic = readings.minOf { it.systolic }
        val maxSystolic = readings.maxOf { it.systolic }
        val minDiastolic = readings.minOf { it.diastolic }
        val maxDiastolic = readings.maxOf { it.diastolic }
        val avgMAP = readings.map { it.meanArterialPressure }.average()
        
        val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 30f, 30f)))
            .useAllAvailableWidth()
        
        listOf("Metric", "Value", "Range").forEach { header ->
            statsTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setBold())
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        
        statsTable.addCell(Cell().add(Paragraph("Total Readings")))
        statsTable.addCell(Cell().add(Paragraph("${readings.size}").setTextAlignment(TextAlignment.CENTER)))
        statsTable.addCell(Cell().add(Paragraph("-").setTextAlignment(TextAlignment.CENTER)))
        
        statsTable.addCell(Cell().add(Paragraph("Average Systolic")))
        statsTable.addCell(Cell().add(Paragraph("${String.format("%.0f", avgSystolic)} mmHg").setTextAlignment(TextAlignment.CENTER)))
        statsTable.addCell(Cell().add(Paragraph("$minSystolic - $maxSystolic mmHg").setTextAlignment(TextAlignment.CENTER)))
        
        statsTable.addCell(Cell().add(Paragraph("Average Diastolic")))
        statsTable.addCell(Cell().add(Paragraph("${String.format("%.0f", avgDiastolic)} mmHg").setTextAlignment(TextAlignment.CENTER)))
        statsTable.addCell(Cell().add(Paragraph("$minDiastolic - $maxDiastolic mmHg").setTextAlignment(TextAlignment.CENTER)))
        
        statsTable.addCell(Cell().add(Paragraph("Average Pulse")))
        statsTable.addCell(Cell().add(Paragraph("${String.format("%.0f", avgPulse)} bpm").setTextAlignment(TextAlignment.CENTER)))
        statsTable.addCell(Cell().add(Paragraph("-").setTextAlignment(TextAlignment.CENTER)))
        
        statsTable.addCell(Cell().add(Paragraph("Mean Arterial Pressure")))
        statsTable.addCell(Cell().add(Paragraph("${String.format("%.1f", avgMAP)} mmHg").setTextAlignment(TextAlignment.CENTER)))
        statsTable.addCell(Cell().add(Paragraph("-").setTextAlignment(TextAlignment.CENTER)))
        
        document.add(statsTable)
    }
    
    private fun addCategoryDistribution(document: Document, readings: List<BloodPressureReading>) {
        document.add(
            Paragraph("Category Distribution")
                .setFontSize(18f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20f)
                .setMarginBottom(10f)
        )
        
        val categories = readings.groupBy { it.category }
        
        val categoryTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 40f)))
            .useAllAvailableWidth()
        
        listOf("Category", "Count", "Percentage").forEach { header ->
            categoryTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setBold())
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        
        BloodPressureCategory.values().forEach { category ->
            val count = categories[category]?.size ?: 0
            val percentage = if (readings.isNotEmpty()) (count.toFloat() / readings.size * 100) else 0f
            
            val categoryColor = when (category) {
                BloodPressureCategory.LOW -> NORMAL_COLOR
                BloodPressureCategory.IDEAL -> NORMAL_COLOR
                BloodPressureCategory.PRE_HIGH -> ELEVATED_COLOR
                BloodPressureCategory.HIGH -> HIGH_COLOR
            }
            
            categoryTable.addCell(
                Cell().add(Paragraph(category.label).setFontColor(categoryColor))
            )
            categoryTable.addCell(
                Cell().add(Paragraph("$count").setTextAlignment(TextAlignment.CENTER))
            )
            categoryTable.addCell(
                Cell().add(Paragraph("${String.format("%.1f", percentage)}%").setTextAlignment(TextAlignment.CENTER))
            )
        }
        
        document.add(categoryTable)
    }
    
    private fun addTrendAnalysis(document: Document, readings: List<BloodPressureReading>) {
        if (readings.size < 7) return
        
        document.add(
            Paragraph("Trend Analysis")
                .setFontSize(18f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20f)
                .setMarginBottom(10f)
        )
        
        val sortedReadings = readings.sortedBy { it.timestamp }
        val firstHalf = sortedReadings.take(sortedReadings.size / 2)
        val secondHalf = sortedReadings.drop(sortedReadings.size / 2)
        
        val firstHalfAvgSystolic = firstHalf.map { it.systolic }.average()
        val secondHalfAvgSystolic = secondHalf.map { it.systolic }.average()
        val systolicTrend = secondHalfAvgSystolic - firstHalfAvgSystolic
        
        val firstHalfAvgDiastolic = firstHalf.map { it.diastolic }.average()
        val secondHalfAvgDiastolic = secondHalf.map { it.diastolic }.average()
        val diastolicTrend = secondHalfAvgDiastolic - firstHalfAvgDiastolic
        
        val trendDescription = when {
            systolicTrend < -5 && diastolicTrend < -3 -> "Blood pressure shows improvement over the period"
            systolicTrend > 5 || diastolicTrend > 3 -> "Blood pressure has increased over the period - please consult your doctor"
            else -> "Blood pressure has remained relatively stable"
        }
        
        document.add(
            Paragraph("Systolic Trend: ${if (systolicTrend >= 0) "+" else ""}${String.format("%.1f", systolicTrend)} mmHg")
                .setFontSize(12f)
        )
        document.add(
            Paragraph("Diastolic Trend: ${if (diastolicTrend >= 0) "+" else ""}${String.format("%.1f", diastolicTrend)} mmHg")
                .setFontSize(12f)
        )
        document.add(
            Paragraph(trendDescription)
                .setFontSize(12f)
                .setItalic()
                .setMarginTop(5f)
        )
    }
    
    private fun addMedicationSection(document: Document, medications: List<Medication>) {
        document.add(
            Paragraph("Current Medications")
                .setFontSize(18f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20f)
                .setMarginBottom(10f)
        )
        
        val medTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 20f, 25f, 25f)))
            .useAllAvailableWidth()
        
        listOf("Medication", "Dosage", "Frequency", "Started").forEach { header ->
            medTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setBold())
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        
        medications.filter { it.isActive }.forEach { med ->
            medTable.addCell(Cell().add(Paragraph(med.name)))
            medTable.addCell(Cell().add(Paragraph(med.dosage).setTextAlignment(TextAlignment.CENTER)))
            medTable.addCell(Cell().add(Paragraph(med.frequency.label).setTextAlignment(TextAlignment.CENTER)))
            medTable.addCell(Cell().add(Paragraph(med.formattedStartDate).setTextAlignment(TextAlignment.CENTER)))
        }
        
        document.add(medTable)
    }
    
    private fun addReadingHistory(document: Document, readings: List<BloodPressureReading>) {
        document.add(
            Paragraph("Reading History")
                .setFontSize(18f)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(20f)
                .setMarginBottom(10f)
        )
        
        val table = Table(UnitValue.createPercentArray(floatArrayOf(25f, 15f, 15f, 15f, 30f)))
            .useAllAvailableWidth()
        
        listOf("Date/Time", "Systolic", "Diastolic", "Pulse", "Category").forEach { header ->
            table.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setBold())
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        
        readings.sortedByDescending { it.timestamp }.take(50).forEach { reading ->
            table.addCell(
                Cell().add(Paragraph(reading.formattedDateTime).setFontSize(9f))
            )
            table.addCell(
                Cell().add(Paragraph("${reading.systolic}").setTextAlignment(TextAlignment.CENTER))
            )
            table.addCell(
                Cell().add(Paragraph("${reading.diastolic}").setTextAlignment(TextAlignment.CENTER))
            )
            table.addCell(
                Cell().add(Paragraph("${reading.pulse}").setTextAlignment(TextAlignment.CENTER))
            )
            
            val categoryColor = when (reading.category) {
                BloodPressureCategory.LOW -> NORMAL_COLOR
                BloodPressureCategory.IDEAL -> NORMAL_COLOR
                BloodPressureCategory.PRE_HIGH -> ELEVATED_COLOR
                BloodPressureCategory.HIGH -> HIGH_COLOR
            }
            
            table.addCell(
                Cell().add(Paragraph(reading.category.label).setFontSize(9f).setFontColor(categoryColor))
            )
        }
        
        document.add(table)
        
        if (readings.size > 50) {
            document.add(
                Paragraph("* Showing most recent 50 of ${readings.size} readings")
                    .setFontSize(10f)
                    .setItalic()
                    .setMarginTop(5f)
            )
        }
    }
    
    private fun addDisclaimer(document: Document) {
        document.add(
            Paragraph("\nDisclaimer: This report is for informational purposes only and should not be used as a substitute for professional medical advice. Always consult your healthcare provider for medical guidance.")
                .setFontSize(8f)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30f)
        )
        
        document.add(
            Paragraph("Generated by Blood Pressure Tracker")
                .setFontSize(8f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10f)
        )
    }
}
