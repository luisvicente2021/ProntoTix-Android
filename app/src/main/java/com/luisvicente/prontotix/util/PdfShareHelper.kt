package com.luisvicente.prontotix.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {

    fun openPdf(
        context: Context,
        filePath: String
    ): Result<Unit> {
        return try {
            val file = File(filePath)

            if (!file.exists()) {
                return Result.failure(
                    Exception("El archivo PDF no existe")
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)

            Result.success(Unit)
        } catch (exception: ActivityNotFoundException) {
            Result.failure(
                Exception("No hay una aplicación instalada para abrir PDF")
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun sharePdf(
        context: Context,
        filePath: String
    ): Result<Unit> {
        return try {
            val file = File(filePath)

            if (!file.exists()) {
                return Result.failure(
                    Exception("El archivo PDF no existe")
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Reporte de entrega"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(
                shareIntent,
                "Compartir reporte"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooserIntent)

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}