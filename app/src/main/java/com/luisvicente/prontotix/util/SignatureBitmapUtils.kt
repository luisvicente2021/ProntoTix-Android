package com.luisvicente.prontotix.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.luisvicente.prontotix.data.model.SignatureData
import java.io.ByteArrayOutputStream

object SignatureBitmapUtils {

    fun toPngBytes(
        signature: SignatureData,
        width: Int = 1000,
        height: Int = 400
    ): ByteArray {

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        // Fondo blanco para que se vea bien en PDF
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 6f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        val path = Path()

        var hasStarted = false

        signature.points.forEach { point ->

            if (point.isNewLine || !hasStarted) {
                path.moveTo(
                    point.x,
                    point.y
                )

                hasStarted = true
            } else {
                path.lineTo(
                    point.x,
                    point.y
                )
            }
        }

        canvas.drawPath(
            path,
            paint
        )

        return ByteArrayOutputStream().use { output ->

            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                output
            )

            output.toByteArray()
        }
    }
}