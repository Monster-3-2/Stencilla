package com.stencilla.app.ml

import android.content.Context
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight on-device first-pass labeler.
 * Returns a rough category guess before the server AI tags the item.
 * Currently returns a placeholder — wire up ML Kit or TFLite here when ready.
 */
@Singleton
class OnDeviceLabeler @Inject constructor() {
    fun labelImage(context: Context, imageUri: Uri): String? {
        // Placeholder [ML-1]: integrate ML Kit Image Labeling here
        // e.g. ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        return null
    }
}
