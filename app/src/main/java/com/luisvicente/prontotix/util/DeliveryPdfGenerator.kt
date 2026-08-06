package com.luisvicente.prontotix.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.luisvicente.prontotix.ui.deliveryreport.DeliveryReportUiState
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeliveryPdfGenerator {

    fun generate(
        context: Context,
        ticketId: Long,
        report: DeliveryReportUiState
    ): Result<File> {
        return try {
            val pdfDocument = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            val lineHeight = 22f

            var pageNumber = 1
            var page = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            var canvas = page.canvas
            var y = 55f

            val titlePaint = Paint().apply {
                textSize = 24f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val subtitlePaint = Paint().apply {
                textSize = 17f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val boldPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val normalPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.DEFAULT
            }

            fun startNewPage() {
                pdfDocument.finishPage(page)

                pageNumber++

                page = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageNumber
                    ).create()
                )

                canvas = page.canvas
                y = 55f
            }

            fun ensureSpace(requiredHeight: Float) {
                if (y + requiredHeight > pageHeight - margin) {
                    startNewPage()
                }
            }

            fun drawLine(
                label: String,
                value: String
            ) {
                ensureSpace(lineHeight)

                canvas.drawText(
                    "$label:",
                    margin,
                    y,
                    boldPaint
                )

                canvas.drawText(
                    value,
                    margin + 115f,
                    y,
                    normalPaint
                )

                y += lineHeight
            }

            canvas.drawText(
                "PRONTOTIX",
                margin,
                y,
                titlePaint
            )

            y += 35f

            canvas.drawText(
                "Reporte de entrega de materiales",
                margin,
                y,
                subtitlePaint
            )

            y += 35f

            val date = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale("es", "MX")
            ).format(Date())

            drawLine(
                label = "Asignación",
                value = "#$ticketId"
            )

            drawLine(
                label = "Fecha",
                value = date
            )

            drawLine(
                label = "Proveedor",
                value = report.provider.ifBlank {
                    "No especificado"
                }
            )

            drawLine(
                label = "Recibió",
                value = report.receiverName
            )

            y += 15f

            canvas.drawText(
                "Materiales",
                margin,
                y,
                subtitlePaint
            )

            y += 28f

            report.items.forEachIndexed { index, item ->
                ensureSpace(105f)

                canvas.drawText(
                    "${index + 1}. ${item.material}",
                    margin,
                    y,
                    boldPaint
                )

                y += lineHeight

                drawLine(
                    label = "Cantidad",
                    value = item.quantity
                )

                drawLine(
                    label = "Precio unitario",
                    value = formatCurrency(
                        item.unitPrice.toDoubleOrNull()
                            ?: 0.0
                    )
                )

                drawLine(
                    label = "Importe",
                    value = formatCurrency(item.total)
                )

                y += 10f
            }

            ensureSpace(65f)

            canvas.drawText(
                "TOTAL: ${formatCurrency(report.grandTotal)}",
                margin,
                y,
                subtitlePaint
            )

            y += 38f

            canvas.drawText(
                "Observaciones",
                margin,
                y,
                subtitlePaint
            )

            y += 25f

            val observations = report.observations.ifBlank {
                "Sin observaciones"
            }

            y = drawWrappedText(
                canvas = canvas,
                text = observations,
                startX = margin,
                startY = y,
                maxWidth = pageWidth - (margin * 2),
                paint = normalPaint,
                lineHeight = 18f,
                onPageRequired = {
                    startNewPage()
                    y
                }
            )

            y += 30f

            canvas.drawText(
                "Generado por ProntoTix",
                margin,
                y,
                normalPaint
            )

            pdfDocument.finishPage(page)

            val reportsDirectory = File(
                context.filesDir,
                "reports"
            ).apply {
                mkdirs()
            }

            val outputFile = File(
                reportsDirectory,
                "reporte_entrega_$ticketId.pdf"
            )

            FileOutputStream(outputFile).use { stream ->
                pdfDocument.writeTo(stream)
            }

            pdfDocument.close()

            Result.success(outputFile)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun formatCurrency(
        amount: Double
    ): String {
        return NumberFormat
            .getCurrencyInstance(
                Locale("es", "MX")
            )
            .format(amount)
    }

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
        onPageRequired: () -> Float
    ): Float {
        var y = startY
        var currentLine = ""

        text.split(" ").forEach { word ->
            val candidate = if (currentLine.isEmpty()) {
                word
            } else {
                "$currentLine $word"
            }

            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                canvas.drawText(
                    currentLine,
                    startX,
                    y,
                    paint
                )

                y += lineHeight
                currentLine = word
            }
        }

        if (currentLine.isNotBlank()) {
            canvas.drawText(
                currentLine,
                startX,
                y,
                paint
            )

            y += lineHeight
        }

        return y
    }
}