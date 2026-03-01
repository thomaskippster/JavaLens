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

    /**
     * Extracts frames from a video at a fixed interval (500ms) for OCR analysis.
     */
    fun extractFrames(videoUri: Uri, intervalMs: Long = 500): Flow<Bitmap> = flow {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLong() ?: 0L

            var currentMillis = 0L
            while (currentMillis < duration) {
                // Extract frame at current timestamp (in microseconds)
                val frame = retriever.getFrameAtTime(
                    currentMillis * 1000, 
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (frame != null) {
                    emit(frame)
                }
                currentMillis += intervalMs
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.IO)
}
