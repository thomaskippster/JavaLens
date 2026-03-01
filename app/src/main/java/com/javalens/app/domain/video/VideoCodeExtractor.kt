package com.javalens.app.domain.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class VideoCodeExtractor(private val context: Context) {
    fun extractFrames(videoUri: Uri, intervalMs: Long = 500): Flow<Bitmap> = flow {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val fpsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            val fps = fpsStr?.toFloatOrNull() ?: 30f
            val totalFrames = ((durationStr?.toLongOrNull() ?: 0L) / 1000f * fps).toInt()
            val framesToSkip = ((intervalMs / 1000f) * fps).toInt().coerceAtLeast(1)

            var currentIndex = 0
            while (currentIndex < totalFrames) {
                val frames = retriever.getFramesAtIndex(currentIndex, 1, MediaMetadataRetriever.BITMAP_CONTROL_DEFAULT)
                if (!frames.isNullOrEmpty()) emit(frames[0])
                currentIndex += framesToSkip
            }
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.IO)
}