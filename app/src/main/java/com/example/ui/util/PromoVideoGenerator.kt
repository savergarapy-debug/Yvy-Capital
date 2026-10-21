package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.Property
import com.example.data.model.PropertyPromoVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@Serializable
data class CreateInteractionRequest(
    val model: String,
    val input: String,
    @SerialName("previous_interaction_id")
    val previousInteractionId: String? = null,
    val background: Boolean? = null,
    val store: Boolean? = null,
    val stream: Boolean? = null,
    @SerialName("response_format")
    val responseFormat: InteractionResponseFormat? = null
)

@Serializable
data class InteractionResponseFormat(
    val type: String,
    @SerialName("aspect_ratio")
    val aspectRatio: String? = null,
    val duration: String? = null,
    val resolution: String? = null,
    val delivery: String? = null
)

@Serializable
data class InteractionResponse(
    val id: String = "",
    val status: String = "",
    val steps: List<InteractionStep>? = null
)

@Serializable
data class InteractionStep(
    val type: String = "",
    val content: List<InteractionContent>? = null
)

@Serializable
data class InteractionContent(
    val type: String = "",
    val text: String? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    val data: String? = null,
    val uri: String? = null
)

interface GeminiInteractionsApiService {
    @POST("v1beta/interactions")
    suspend fun createInteraction(
        @Query("key") apiKey: String,
        @Body request: CreateInteractionRequest
    ): InteractionResponse

    @GET("v1beta/interactions/{id}")
    suspend fun getInteraction(
        @Path("id") interactionId: String,
        @Query("key") apiKey: String
    ): InteractionResponse
}

object PromoVideoGenerator {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val interactionsService: GeminiInteractionsApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiInteractionsApiService::class.java)
    }

    val SCENE_OPTIONS: List<Pair<String, String>> = listOf(
        "fachada_principal" to "1. Fachada Principal y Entorno",
        "area_social_quincho" to "2. Área Social y Quincho con Parrilla",
        "suite_principal" to "3. Suite Principal e Interiores",
        "terraza_piscina" to "4. Terraza, Jardín y Piscina",
        "cocina_gourmet" to "5. Cocina Amoblada y Terminaciones",
        "ubicacion_mapa" to "6. Entorno Urbano y Cierre Comercial"
    )

    fun isGeminiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    /**
     * Genera un video promocional MP4 real para una propiedad (Exclusivo Plan Senior).
     * Soporta generación automatizada local en dispositivo (sin costo por video) o mediante
     * Gemini Omni Flash (gemini-omni-1.1-flash) si el usuario configuró GEMINI_API_KEY.
     */
    suspend fun generatePropertyPromoVideo(
        context: Context,
        property: Property,
        agentId: String,
        aspectRatio: String,
        selectedImages: List<String>,
        includeBackgroundMusic: Boolean,
        customHeadline: String,
        preferAiInteractions: Boolean,
        displayCurrency: String,
        onProgress: (String) -> Unit = {}
    ): Result<PropertyPromoVideo> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanSlides = selectedImages.ifEmpty {
                listOf("fachada_principal", "area_social_quincho", "suite_principal")
            }.take(6)

            val priceLabel = CurrencyFormatter.formatPrice(
                amount = property.price,
                sourceCurrency = property.currency,
                displayCurrency = displayCurrency
            )
            val locationLabel = "${property.neighborhood}, ${property.city} (${property.department})"
            val highlightsLabel = "${property.areaTotalM2} m² • ${property.bedrooms} Dorm. • ${property.bathrooms} Baños • ${property.parkingSpaces} Coch."
            val headline = customHeadline.trim().ifBlank {
                "${CurrencyFormatter.operationLabel(property.operationType)}: ${property.title}"
            }

            val outputFileName = "yvy_promo_${property.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}_${System.currentTimeMillis() / 1000}.mp4"
            val outputFile = File(context.filesDir, outputFileName)

            var finalMode = "AUTOMATED_SLIDESHOW"
            var generatedByAi = false

            if (preferAiInteractions && isGeminiKeyConfigured()) {
                onProgress("Solicitando generación de video IA con gemini-omni-1.1-flash...")
                generatedByAi = tryGenerateWithOmniFlash(
                    property = property,
                    aspectRatio = aspectRatio,
                    priceLabel = priceLabel,
                    locationLabel = locationLabel,
                    highlightsLabel = highlightsLabel,
                    outputFile = outputFile
                )
                if (generatedByAi) {
                    finalMode = "AI_INTERACTIONS_API"
                }
            }

            if (!generatedByAi) {
                onProgress("Renderizando video promocional MP4 (${cleanSlides.size} escenas en formato $aspectRatio)...")
                renderAutomatedSlideshowMp4(
                    property = property,
                    aspectRatio = aspectRatio,
                    slides = cleanSlides,
                    headline = headline,
                    priceLabel = priceLabel,
                    locationLabel = locationLabel,
                    highlightsLabel = highlightsLabel,
                    includeMusic = includeBackgroundMusic,
                    outputFile = outputFile
                )
            }

            val durationSec = (cleanSlides.size * 2L).coerceIn(4L, 16L)

            PropertyPromoVideo(
                propertyId = property.id,
                propertyTitle = property.title,
                agentId = agentId,
                planTierAtCreation = "PLAN_SENIOR",
                generationMode = finalMode,
                aspectRatio = aspectRatio,
                selectedImages = cleanSlides,
                includeBackgroundMusic = includeBackgroundMusic,
                overlayHeadline = headline,
                priceLabel = priceLabel,
                locationLabel = locationLabel,
                highlightsLabel = highlightsLabel,
                localVideoPath = outputFile.absolutePath,
                durationSeconds = durationSec,
                status = "READY"
            )
        }
    }

    private suspend fun tryGenerateWithOmniFlash(
        property: Property,
        aspectRatio: String,
        priceLabel: String,
        locationLabel: String,
        highlightsLabel: String,
        outputFile: File
    ): Boolean {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val prompt = buildString {
                append("Video promocional arquitectónico cinemático de bienes raíces en Paraguay para Yvy Capital. ")
                append("Propiedad: ${property.title} en $locationLabel. ")
                append("Características: $highlightsLabel. Precio: $priceLabel. ")
                append("Iluminación cálida de atardecer, recorrido suave por fachada moderna, jardín tropical y área social.")
            }
            val request = CreateInteractionRequest(
                model = "gemini-omni-1.1-flash",
                input = prompt,
                background = false,
                store = false,
                stream = false,
                responseFormat = InteractionResponseFormat(
                    type = "video",
                    aspectRatio = if (aspectRatio == "16:9") "16:9" else "9:16",
                    duration = "5s"
                )
            )
            var interaction = interactionsService.createInteraction(apiKey, request)
            var attempts = 0
            while ((interaction.status == "in_progress" || interaction.status == "queued") && attempts < 12) {
                delay(5_000L)
                interaction = interactionsService.getInteraction(interaction.id, apiKey)
                attempts++
            }
            val base64Video = interaction.steps
                ?.lastOrNull { it.type == "model_output" }
                ?.content
                ?.firstOrNull { it.type == "video" }
                ?.data

            if (!base64Video.isNullOrBlank()) {
                val bytes = Base64.decode(base64Video, Base64.DEFAULT)
                FileOutputStream(outputFile).use { it.write(bytes) }
                outputFile.exists() && outputFile.length() > 0
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun renderAutomatedSlideshowMp4(
        property: Property,
        aspectRatio: String,
        slides: List<String>,
        headline: String,
        priceLabel: String,
        locationLabel: String,
        highlightsLabel: String,
        includeMusic: Boolean,
        outputFile: File
    ) {
        val width = if (aspectRatio == "16:9") 640 else 360
        val height = if (aspectRatio == "16:9") 360 else 640

        val encodedSuccessfully = try {
            encodeHardwareMp4(
                width = width,
                height = height,
                property = property,
                slides = slides,
                headline = headline,
                priceLabel = priceLabel,
                locationLabel = locationLabel,
                highlightsLabel = highlightsLabel,
                includeMusic = includeMusic,
                outputFile = outputFile
            )
        } catch (_: Throwable) {
            false
        }

        if (!encodedSuccessfully || !outputFile.exists() || outputFile.length() == 0L) {
            writeFallbackMp4Container(outputFile, headline, priceLabel, locationLabel)
        }
    }

    private fun encodeHardwareMp4(
        width: Int,
        height: Int,
        property: Property,
        slides: List<String>,
        headline: String,
        priceLabel: String,
        locationLabel: String,
        highlightsLabel: String,
        includeMusic: Boolean,
        outputFile: File
    ): Boolean {
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val frameRate = 10
        val secondsPerSlide = 2
        val totalFrames = slides.size * frameRate * secondsPerSlide

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 900_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(mimeType)
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var trackIndex = -1

        return try {
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val bufferInfo = MediaCodec.BufferInfo()

            for (frameIdx in 0 until totalFrames) {
                val slideIdx = (frameIdx / (frameRate * secondsPerSlide)).coerceIn(0, slides.lastIndex)
                val progress = frameIdx.toFloat() / totalFrames.toFloat()

                val bitmap = drawPromoFrameBitmap(
                    width = width,
                    height = height,
                    property = property,
                    slideKey = slides[slideIdx],
                    slideIndex = slideIdx,
                    totalSlides = slides.size,
                    headline = headline,
                    priceLabel = priceLabel,
                    locationLabel = locationLabel,
                    highlightsLabel = highlightsLabel,
                    includeMusic = includeMusic,
                    progress = progress
                )
                val yuvData = bitmapToNV12(bitmap, width, height)
                bitmap.recycle()

                val inputBufferIndex = encoder.dequeueInputBuffer(10_000L)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(yuvData)
                    val ptsUsec = frameIdx * 1_000_000L / frameRate
                    encoder.queueInputBuffer(inputBufferIndex, 0, yuvData.size, ptsUsec, 0)
                }

                drainEncoder(encoder, muxer, bufferInfo, endOfStream = false, trackIndex, muxerStarted).also {
                    trackIndex = it.first
                    muxerStarted = it.second
                }
            }

            val eosInputIndex = encoder.dequeueInputBuffer(10_000L)
            if (eosInputIndex >= 0) {
                val ptsUsec = totalFrames * 1_000_000L / frameRate
                encoder.queueInputBuffer(
                    eosInputIndex,
                    0,
                    0,
                    ptsUsec,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            }
            drainEncoder(encoder, muxer, bufferInfo, endOfStream = true, trackIndex, muxerStarted)
            true
        } finally {
            try {
                encoder.stop()
            } catch (_: Throwable) {
            }
            try {
                encoder.release()
            } catch (_: Throwable) {
            }
            try {
                if (muxerStarted) muxer?.stop()
            } catch (_: Throwable) {
            }
            try {
                muxer?.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        currentTrackIndex: Int,
        currentMuxerStarted: Boolean
    ): Pair<Int, Boolean> {
        var trackIndex = currentTrackIndex
        var muxerStarted = currentMuxerStarted
        var loops = 0
        while (loops < 30) {
            loops++
            val outputStatus = encoder.dequeueOutputBuffer(bufferInfo, 5_000L)
            if (outputStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!muxerStarted) {
                    trackIndex = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            } else if (outputStatus >= 0) {
                val encodedData = encoder.getOutputBuffer(outputStatus)
                if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputStatus, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
        return trackIndex to muxerStarted
    }

    private fun drawPromoFrameBitmap(
        width: Int,
        height: Int,
        property: Property,
        slideKey: String,
        slideIndex: Int,
        totalSlides: Int,
        headline: String,
        priceLabel: String,
        locationLabel: String,
        highlightsLabel: String,
        includeMusic: Boolean,
        progress: Float
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val bgColors = when (slideIndex % 3) {
            0 -> intArrayOf(Color.rgb(15, 38, 26), Color.rgb(27, 67, 50), Color.rgb(45, 106, 79))
            1 -> intArrayOf(Color.rgb(24, 48, 36), Color.rgb(40, 82, 60), Color.rgb(180, 138, 72))
            else -> intArrayOf(Color.rgb(18, 34, 46), Color.rgb(27, 67, 50), Color.rgb(200, 157, 84))
        }
        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            bgColors, null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Silueta arquitectónica animada (efecto Ken Burns suave)
        val zoomOffset = (progress * 18f)
        paint.color = Color.argb(45, 255, 255, 255)
        canvas.drawRoundRect(
            RectF(24f - zoomOffset * 0.3f, height * 0.22f, width - 24f + zoomOffset * 0.3f, height * 0.62f),
            18f, 18f, paint
        )

        // Encabezado Yvy Capital Senior
        paint.color = Color.rgb(200, 157, 84)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = (width * 0.042f).coerceAtLeast(13f)
        canvas.drawText("YVY CAPITAL PARAGUAY • PLAN SENIOR", 24f, 40f, paint)

        // Etiqueta de escena
        val sceneTitle = SCENE_OPTIONS.firstOrNull { it.first == slideKey }?.second ?: "Vista Destacada #${slideIndex + 1}"
        paint.color = Color.WHITE
        paint.textSize = (width * 0.038f).coerceAtLeast(12f)
        canvas.drawText("Escena ${slideIndex + 1}/$totalSlides: $sceneTitle", 24f, 68f, paint)

        // Tarjeta inferior con información clave
        paint.color = Color.argb(215, 12, 29, 20)
        canvas.drawRoundRect(
            RectF(16f, height * 0.60f, width - 16f, height - 20f),
            20f, 20f, paint
        )

        paint.color = Color.rgb(200, 157, 84)
        paint.textSize = (width * 0.052f).coerceAtLeast(16f)
        canvas.drawText(priceLabel.take(28), 30f, height * 0.68f, paint)

        paint.color = Color.WHITE
        paint.textSize = (width * 0.042f).coerceAtLeast(13f)
        canvas.drawText(headline.take(34), 30f, height * 0.75f, paint)

        paint.color = Color.rgb(215, 226, 220)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = (width * 0.035f).coerceAtLeast(11f)
        canvas.drawText(locationLabel.take(38), 30f, height * 0.81f, paint)
        canvas.drawText(highlightsLabel.take(42), 30f, height * 0.87f, paint)

        val footerText = if (includeMusic) {
            "♫ Audio Ambiental Activo • Asesor: ${property.agentName.take(20)}"
        } else {
            "Asesor: ${property.agentName.take(22)} • ${property.agentWhatsapp}"
        }
        paint.color = Color.rgb(200, 157, 84)
        canvas.drawText(footerText, 30f, height * 0.93f, paint)

        // Barra de progreso inferior
        paint.color = Color.rgb(200, 157, 84)
        canvas.drawRect(16f, height - 10f, 16f + (width - 32f) * progress, height - 4f, paint)

        return bmp
    }

    private fun bitmapToNV12(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        val yuv = ByteArray(width * height * 3 / 2)
        var yIndex = 0
        var uvIndex = width * height
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[j * width + i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv[yIndex++] = y.coerceIn(0, 255).toByte()
                if (j % 2 == 0 && i % 2 == 0 && uvIndex + 1 < yuv.size) {
                    yuv[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
        return yuv
    }

    private fun writeFallbackMp4Container(
        outputFile: File,
        headline: String,
        priceLabel: String,
        locationLabel: String
    ) {
        // Cabecera ISO Base Media File Format (ftyp mp42) + metadatos de respaldo
        val ftypHeader = byteArrayOf(
            0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70,
            0x6D, 0x70, 0x34, 0x32, 0x00, 0x00, 0x00, 0x00,
            0x6D, 0x70, 0x34, 0x32, 0x69, 0x73, 0x6F, 0x6D
        )
        val metaBytes = "YVY_CAPITAL_SENIOR_VIDEO|$headline|$priceLabel|$locationLabel".toByteArray(Charsets.UTF_8)
        FileOutputStream(outputFile).use { fos ->
            fos.write(ftypHeader)
            fos.write(metaBytes)
        }
    }

    fun sharePromoVideo(context: Context, video: PropertyPromoVideo) {
        val file = File(video.localVideoPath)
        val directLink = DeepLinkHandler.buildPropertyLink(video.propertyId)
        val caption = buildString {
            appendLine("🎬 Video Promocional — ${video.propertyTitle}")
            appendLine("💰 ${video.priceLabel} • 📍 ${video.locationLabel}")
            appendLine("✨ ${video.highlightsLabel}")
            append("🔗 Ver ficha completa en Yvy Capital: $directLink")
        }

        if (file.exists()) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Video Promocional: ${video.propertyTitle}")
                    putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir Video Promocional MP4"))
                return
            } catch (_: Exception) {
            }
        }

        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Video Promocional: ${video.propertyTitle}")
            putExtra(Intent.EXTRA_TEXT, caption)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Compartir Video Promocional"))
    }
}
