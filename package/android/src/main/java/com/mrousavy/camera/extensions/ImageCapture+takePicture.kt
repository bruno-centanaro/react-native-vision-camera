package com.mrousavy.camera.extensions

import android.media.MediaActionSound
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend inline fun ImageCapture.takePicture(enableShutterSound: Boolean, executor: Executor): ImageProxy =
  suspendCancellableCoroutine { continuation ->
    val shutterSound = if (enableShutterSound) MediaActionSound() else null
    shutterSound?.load(MediaActionSound.SHUTTER_CLICK)

    takePicture(
      executor,
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureStarted() {
          super.onCaptureStarted()
          if (enableShutterSound) {
            shutterSound?.play(MediaActionSound.SHUTTER_CLICK)
          }
        }

        override fun onCaptureSuccess(image: ImageProxy) {
          super.onCaptureSuccess(image)
          if (continuation.isActive) {
            continuation.resume(image)
          }
        }

        override fun onError(exception: ImageCaptureException) {
          super.onError(exception)
          if (continuation.isActive) {
            continuation.resumeWithException(exception)
          }
        }
      }
    )
  }