package com.luisvicente.prontotix.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.luisvicente.prontotix.data.model.EvidencePhoto
import com.luisvicente.prontotix.data.model.SignatureData
import com.luisvicente.prontotix.ui.deliveryreport.DeliveryReportUiState
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

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
            val contentWidth =
                pageWidth - (margin * 2)

            var pageNumber = 1

            var page = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            var canvas = page.canvas
            var y = 50f

            /*
             * ESTILOS
             */

            val titlePaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 24f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val reportTitlePaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 17f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val sectionPaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 15f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val boldPaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 11f
                typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            }

            val normalPaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 11f
                typeface = Typeface.DEFAULT
            }

            val smallPaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                textSize = 9f
                typeface = Typeface.DEFAULT
            }

            val linePaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                strokeWidth = 1f
                color = Color.LTGRAY
            }

            /*
             * NUEVA PÁGINA
             */

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
                y = 50f

                canvas.drawText(
                    "PRONTOTIX",
                    margin,
                    y,
                    boldPaint
                )

                canvas.drawText(
                    "Reporte #$ticketId",
                    pageWidth - margin - 80f,
                    y,
                    smallPaint
                )

                y += 25f
            }

            fun ensureSpace(
                requiredHeight: Float
            ) {

                if (
                    y + requiredHeight >
                    pageHeight - 50f
                ) {
                    startNewPage()
                }
            }

            /*
             * LÍNEA DE INFORMACIÓN
             */

            fun drawInfo(
                label: String,
                value: String
            ) {

                ensureSpace(20f)

                canvas.drawText(
                    label,
                    margin,
                    y,
                    boldPaint
                )

                canvas.drawText(
                    value,
                    margin + 105f,
                    y,
                    normalPaint
                )

                y += 20f
            }

            /*
             * TÍTULO DE SECCIÓN
             */

            fun drawSection(
                title: String
            ) {

                ensureSpace(40f)

                y += 12f

                canvas.drawText(
                    title,
                    margin,
                    y,
                    sectionPaint
                )

                y += 8f

                canvas.drawLine(
                    margin,
                    y,
                    pageWidth - margin,
                    y,
                    linePaint
                )

                y += 20f
            }

            /*
             * TEXTO MULTILÍNEA
             */

            fun drawWrappedText(
                text: String,
                paint: Paint = normalPaint,
                lineHeight: Float = 17f
            ) {

                val paragraphs =
                    text.split("\n")

                paragraphs.forEach { paragraph ->

                    if (paragraph.isBlank()) {

                        ensureSpace(lineHeight)
                        y += lineHeight

                    } else {

                        var currentLine = ""

                        paragraph
                            .split(" ")
                            .forEach { word ->

                                val candidate =
                                    if (
                                        currentLine.isEmpty()
                                    ) {
                                        word
                                    } else {
                                        "$currentLine $word"
                                    }

                                if (
                                    paint.measureText(
                                        candidate
                                    ) <= contentWidth
                                ) {

                                    currentLine =
                                        candidate

                                } else {

                                    ensureSpace(
                                        lineHeight
                                    )

                                    canvas.drawText(
                                        currentLine,
                                        margin,
                                        y,
                                        paint
                                    )

                                    y += lineHeight

                                    currentLine =
                                        word
                                }
                            }

                        if (
                            currentLine.isNotBlank()
                        ) {

                            ensureSpace(
                                lineHeight
                            )

                            canvas.drawText(
                                currentLine,
                                margin,
                                y,
                                paint
                            )

                            y += lineHeight
                        }
                    }
                }
            }

            /*
             * IMAGEN DESDE URI
             */

            fun loadBitmap(
                photo: EvidencePhoto
            ): Bitmap? {

                return try {

                    val uri =
                        Uri.parse(photo.uri)

                    context
                        .contentResolver
                        .openInputStream(uri)
                        ?.use {
                            BitmapFactory
                                .decodeStream(it)
                        }

                } catch (_: Exception) {
                    null
                }
            }

            /*
             * DIBUJAR IMAGEN
             */

            fun drawPhoto(
                photo: EvidencePhoto,
                label: String
            ) {

                val bitmap =
                    loadBitmap(photo)
                        ?: return

                val maxWidth =
                    contentWidth

                val maxHeight =
                    360f

                val scale =
                    min(
                        maxWidth /
                                bitmap.width.toFloat(),
                        maxHeight /
                                bitmap.height.toFloat()
                    )

                val width =
                    bitmap.width * scale

                val height =
                    bitmap.height * scale

                ensureSpace(
                    height + 45f
                )

                canvas.drawText(
                    label,
                    margin,
                    y,
                    boldPaint
                )

                y += 15f

                val left =
                    margin +
                            (contentWidth - width) / 2f

                val destination =
                    RectF(
                        left,
                        y,
                        left + width,
                        y + height
                    )

                canvas.drawBitmap(
                    bitmap,
                    null,
                    destination,
                    null
                )

                y += height + 20f

                bitmap.recycle()
            }

            /*
             * FIRMA
             */

            fun drawSignature(
                signature: SignatureData
            ) {

                if (
                    signature.points.isEmpty()
                ) {
                    return
                }

                ensureSpace(180f)

                val boxHeight = 120f

                val boxTop = y

                val signaturePaint =
                    Paint(
                        Paint.ANTI_ALIAS_FLAG
                    ).apply {
                        color = Color.BLACK
                        strokeWidth = 2f
                        style =
                            Paint.Style.STROKE
                    }

                val borderPaint =
                    Paint(
                        Paint.ANTI_ALIAS_FLAG
                    ).apply {
                        color =
                            Color.LTGRAY
                        strokeWidth = 1f
                        style =
                            Paint.Style.STROKE
                    }

                canvas.drawRect(
                    margin,
                    boxTop,
                    pageWidth - margin,
                    boxTop + boxHeight,
                    borderPaint
                )

                val points =
                    signature.points

                val drawablePoints =
                    points.filter {
                        !it.isNewLine
                    }

                if (
                    drawablePoints.isNotEmpty()
                ) {

                    val minX =
                        drawablePoints
                            .minOf { it.x }

                    val maxX =
                        drawablePoints
                            .maxOf { it.x }

                    val minY =
                        drawablePoints
                            .minOf { it.y }

                    val maxY =
                        drawablePoints
                            .maxOf { it.y }

                    val sourceWidth =
                        max(
                            maxX - minX,
                            1f
                        )

                    val sourceHeight =
                        max(
                            maxY - minY,
                            1f
                        )

                    val availableWidth =
                        contentWidth - 40f

                    val availableHeight =
                        boxHeight - 30f

                    val scale =
                        min(
                            availableWidth /
                                    sourceWidth,
                            availableHeight /
                                    sourceHeight
                        )

                    val offsetX =
                        margin +
                                (
                                        contentWidth -
                                                sourceWidth * scale
                                        ) / 2f

                    val offsetY =
                        boxTop +
                                (
                                        boxHeight -
                                                sourceHeight * scale
                                        ) / 2f

                    var previousX: Float? =
                        null

                    var previousY: Float? =
                        null

                    points.forEach { point ->

                        if (
                            point.isNewLine
                        ) {

                            previousX = null
                            previousY = null

                        } else {

                            val x =
                                offsetX +
                                        (
                                                point.x -
                                                        minX
                                                ) * scale

                            val pointY =
                                offsetY +
                                        (
                                                point.y -
                                                        minY
                                                ) * scale

                            if (
                                previousX != null &&
                                previousY != null
                            ) {

                                canvas.drawLine(
                                    previousX!!,
                                    previousY!!,
                                    x,
                                    pointY,
                                    signaturePaint
                                )
                            }

                            previousX = x
                            previousY = pointY
                        }
                    }
                }

                y += boxHeight + 12f

                canvas.drawText(
                    report.receiverName
                        .ifBlank {
                            "Nombre del receptor"
                        },
                    margin,
                    y,
                    boldPaint
                )

                y += 15f

                canvas.drawText(
                    "Firma de recibido",
                    margin,
                    y,
                    smallPaint
                )

                y += 25f
            }

            /*
             * ENCABEZADO
             */

            canvas.drawText(
                "PRONTOTIX",
                margin,
                y,
                titlePaint
            )

            y += 32f

            canvas.drawText(
                "Reporte de entrega de materiales",
                margin,
                y,
                reportTitlePaint
            )

            y += 12f

            canvas.drawLine(
                margin,
                y,
                pageWidth - margin,
                y,
                linePaint
            )

            y += 28f

            val date =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale(
                        "es",
                        "MX"
                    )
                ).format(Date())

            drawInfo(
                "Asignación:",
                "#$ticketId"
            )

            drawInfo(
                "Fecha:",
                date
            )

            drawInfo(
                "Proveedor:",
                report.provider
                    .ifBlank {
                        "No especificado"
                    }
            )

            drawInfo(
                "Recibió:",
                report.receiverName
                    .ifBlank {
                        "No especificado"
                    }
            )

            /*
             * MATERIALES
             */

            drawSection(
                "Materiales entregados"
            )

            report.items
                .forEachIndexed {
                        index,
                        item ->

                    ensureSpace(90f)

                    canvas.drawText(
                        "${index + 1}. ${item.material}",
                        margin,
                        y,
                        boldPaint
                    )

                    y += 20f

                    drawInfo(
                        "Cantidad:",
                        item.quantity
                    )

                    drawInfo(
                        "Precio unitario:",
                        formatCurrency(
                            item.unitPrice
                                .toDoubleOrNull()
                                ?: 0.0
                        )
                    )

                    drawInfo(
                        "Importe:",
                        formatCurrency(
                            item.total
                        )
                    )

                    y += 8f
                }

            ensureSpace(45f)

            canvas.drawLine(
                margin,
                y,
                pageWidth - margin,
                y,
                linePaint
            )

            y += 24f

            canvas.drawText(
                "TOTAL: ${
                    formatCurrency(
                        report.grandTotal
                    )
                }",
                margin,
                y,
                reportTitlePaint
            )

            y += 15f

            /*
             * OBSERVACIONES
             */

            drawSection(
                "Observaciones"
            )

            drawWrappedText(
                report.observations
                    .ifBlank {
                        "Sin observaciones"
                    }
            )

            /*
             * RECIBO
             */

            report.receiptPhoto?.let {

                drawSection(
                    "Comprobante / nota de compra"
                )

                drawPhoto(
                    photo = it,
                    label =
                        "Comprobante de compra"
                )
            }

            /*
             * EVIDENCIAS
             */

            if (
                report.evidencePhotos
                    .isNotEmpty()
            ) {

                drawSection(
                    "Evidencias de entrega"
                )

                report.evidencePhotos
                    .forEachIndexed {
                            index,
                            photo ->

                        drawPhoto(
                            photo = photo,
                            label =
                                "Evidencia ${index + 1}"
                        )
                    }
            }

            /*
             * FIRMA
             */

            report.signature?.let {

                drawSection(
                    "Recepción"
                )

                drawSignature(it)
            }

            /*
             * PIE
             */

            ensureSpace(50f)

            y += 10f

            canvas.drawLine(
                margin,
                y,
                pageWidth - margin,
                y,
                linePaint
            )

            y += 18f

            canvas.drawText(
                "Generado por ProntoTix",
                margin,
                y,
                smallPaint
            )

            canvas.drawText(
                "Diligencia #$ticketId",
                pageWidth - margin - 90f,
                y,
                smallPaint
            )

            /*
             * GUARDAR
             */

            pdfDocument.finishPage(page)

            val reportsDirectory =
                File(
                    context.filesDir,
                    "reports"
                ).apply {
                    mkdirs()
                }

            val outputFile =
                File(
                    reportsDirectory,
                    "reporte_entrega_$ticketId.pdf"
                )

            FileOutputStream(
                outputFile
            ).use { stream ->

                pdfDocument.writeTo(
                    stream
                )
            }

            pdfDocument.close()

            Result.success(
                outputFile
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    private fun formatCurrency(
        amount: Double
    ): String {

        return NumberFormat
            .getCurrencyInstance(
                Locale(
                    "es",
                    "MX"
                )
            )
            .format(amount)
    }
}