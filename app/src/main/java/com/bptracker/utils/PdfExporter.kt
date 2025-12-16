package com.bptracker.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bptracker.data.model.BloodPressureReading
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

object PdfExporter {
    
    fun exportReadings(context: Context, readings: List<BloodPressureReading>) {
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
            
            document.add(
                Paragraph("Blood Pressure Report")
                    .setFontSize(24f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(
                Paragraph("Generated on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            if (readings.isNotEmpty()) {
                val avgSystolic = readings.map { it.systolic }.average()
                val avgDiastolic = readings.map { it.diastolic }.average()
                val avgPulse = readings.map { it.pulse }.average()
                
                document.add(
                    Paragraph("Summary")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(10f)
                )
                
                document.add(
                    Paragraph("Total Readings: ${readings.size}")
                        .setFontSize(12f)
                )
                document.add(
                    Paragraph("Average: %.0f/%.0f mmHg, %.0f bpm".format(avgSystolic, avgDiastolic, avgPulse))
                        .setFontSize(12f)
                        .setMarginBottom(20f)
                )
            }
            
            document.add(
                Paragraph("Reading History")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginTop(10f)
                    .setMarginBottom(10f)
            )
            
            val table = Table(UnitValue.createPercentArray(floatArrayOf(25f, 15f, 15f, 15f, 30f)))
                .useAllAvailableWidth()
            
            listOf("Date/Time", "Systolic", "Diastolic", "Pulse", "Category").forEach { header ->
                table.addHeaderCell(
                    Cell()
                        .add(Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }
            
            readings.forEach { reading ->
                table.addCell(
                    Cell().add(Paragraph(reading.formattedDateTime).setFontSize(10f))
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
                table.addCell(
                    Cell().add(Paragraph(reading.category.label).setFontSize(10f))
                )
            }
            
            document.add(table)
            
            document.add(
                Paragraph("\nDisclaimer: This report is for informational purposes only and should not be used as a substitute for professional medical advice.")
                    .setFontSize(8f)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20f)
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
}
