package com.stencilla.app.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Instant, fully offline rough label for a freshly captured clothing photo.
 * This is intentionally generic (ML Kit's bundled classifier, no custom model to source/train).
 * The authoritative category/color/formality tags come from the cloud Claude vision call on the
 * backend right after upload - this on-device pass only gives the user immediate feedback while
 * that network call is in flight.
 */
@Singleton
class OnDeviceLabeler @Inject constructor() {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun labelImage(context: Context, uri: Uri): String? = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val best = labels.maxByOrNull { it.confidence }
                    cont.resume(best?.text)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }
}
