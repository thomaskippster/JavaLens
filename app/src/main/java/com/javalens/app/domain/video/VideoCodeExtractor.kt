package com.javalens.app.domain.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

sealed class ExtractionEvent {
    data class Progress(val progress: Float) : ExtractionEvent()
    data class Frame(val bitmap: Bitmap) : ExtractionEvent()
}

class VideoCodeExtractor(private val context: Context) {
    fun extractFrames(videoUri: Uri, intervalMs: Long = 500): Flow<ExtractionEvent> = flow {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            
            // FPS detection is tricky, we'll estimate or use common default
            val fpsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            val fps = fpsStr?.toFloatOrNull() ?: 30f
            
            val totalFrames = (duration / 1000f * fps).toInt()
            val framesToSkip = ((intervalMs / 1000f) * fps).toInt().coerceAtLeast(1)

            var currentIndex = 0
            while (currentIndex < totalFrames) {
                val frames = retriever.getFramesAtIndex(currentIndex, 1)
                if (!frames.isNullOrEmpty()) {
                    emit(ExtractionEvent.Frame(frames[0]))
                }
                
                val currentProgress = (currentIndex.toFloat() / totalFrames).coerceIn(0f, 1f)
                emit(ExtractionEvent.Progress(currentProgress))
                
                currentIndex += framesToSkip
            }
            emit(ExtractionEvent.Progress(1f))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
    }.flowOn(Dispatchers.IO)
}
