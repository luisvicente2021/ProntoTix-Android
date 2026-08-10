package com.luisvicente.prontotix.data.repository

import android.content.Context
import android.net.Uri
import com.luisvicente.prontotix.data.remote.SupabaseStorageClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class SupabaseStorageRepository {

    suspend fun uploadReceipt(
        context: Context,
        ticketId: Long,
        uri: Uri,
        accessToken: String,
        publishableKey: String
    ): Result<String> {
        return try {
            val contentResolver =
                context.contentResolver

            val bytes = contentResolver
                .openInputStream(uri)
                ?.use { stream ->
                    stream.readBytes()
                }
                ?: return Result.failure(
                    Exception(
                        "No fue posible leer la fotografía"
                    )
                )

            val mimeType =
                contentResolver.getType(uri)
                    ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val path =
                "ticket-$ticketId/receipt.$extension"

            val requestBody = bytes.toRequestBody(
                mimeType.toMediaType()
            )

            val response =
                SupabaseStorageClient.api()
                    .uploadFile(
                        authorization =
                            "Bearer $accessToken",
                        apiKey = publishableKey,
                        contentType = mimeType,
                        bucket = "delivery-reports",
                        path = path,
                        body = requestBody
                    )

            if (response.isSuccessful) {
                Result.success(path)
            } else {
                Result.failure(
                    Exception(
                        "Error al subir recibo: " +
                                response.code()
                    )
                )
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun uploadEvidence(
        context: Context,
        ticketId: Long,
        uri: Uri,
        accessToken: String,
        publishableKey: String,
        index: Int
    ): Result<String> {
        return try {
            val contentResolver = context.contentResolver

            val bytes = contentResolver
                .openInputStream(uri)
                ?.use { stream ->
                    stream.readBytes()
                }
                ?: return Result.failure(
                    Exception("No fue posible leer la evidencia")
                )

            val mimeType =
                contentResolver.getType(uri)
                    ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val uniqueId = System.currentTimeMillis()

            val path =
                "ticket-$ticketId/evidence-$uniqueId-$index.$extension"

            val requestBody = bytes.toRequestBody(
                mimeType.toMediaType()
            )

            val response =
                SupabaseStorageClient.api()
                    .uploadFile(
                        authorization = "Bearer $accessToken",
                        apiKey = publishableKey,
                        contentType = mimeType,
                        bucket = "delivery-reports",
                        path = path,
                        body = requestBody
                    )

            if (response.isSuccessful) {
                Result.success(path)
            } else {
                Result.failure(
                    Exception(
                        "Error al subir evidencia: ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun uploadSignature(
        ticketId: Long,
        signatureBytes: ByteArray,
        accessToken: String,
        publishableKey: String
    ): Result<String> {
        return try {
            val path = "ticket-$ticketId/signature.png"

            val requestBody = signatureBytes.toRequestBody(
                "image/png".toMediaType()
            )

            val response =
                SupabaseStorageClient.api()
                    .uploadFile(
                        authorization = "Bearer $accessToken",
                        apiKey = publishableKey,
                        contentType = "image/png",
                        bucket = "delivery-reports",
                        path = path,
                        body = requestBody
                    )

            if (response.isSuccessful) {
                Result.success(path)
            } else {
                Result.failure(
                    Exception(
                        "Error al subir firma: ${response.code()}"
                    )
                )
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}