package com.luisvicente.prontotix.ui.deliveryreport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.luisvicente.prontotix.data.model.SignatureData
import com.luisvicente.prontotix.data.model.SignaturePoint

@Composable
fun SignaturePad(
    onSignatureChanged: (SignatureData) -> Unit,
    modifier: Modifier = Modifier
) {
    val points = remember {
        mutableStateListOf<SignaturePoint>()
    }

    val lineColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        points.add(
                            SignaturePoint(
                                x = offset.x,
                                y = offset.y,
                                isNewLine = true
                            )
                        )

                        onSignatureChanged(
                            SignatureData(points.toList())
                        )
                    },
                    onDrag = { change, _ ->
                        points.add(
                            SignaturePoint(
                                x = change.position.x,
                                y = change.position.y
                            )
                        )

                        onSignatureChanged(
                            SignatureData(points.toList())
                        )
                    }
                )
            }
    ) {
        val path = Path()
        var hasStarted = false

        points.forEach { point ->
            if (point.isNewLine || !hasStarted) {
                path.moveTo(point.x, point.y)
                hasStarted = true
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round
            )
        )
    }
}