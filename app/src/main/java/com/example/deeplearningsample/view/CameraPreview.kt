package com.example.deeplearningsample.view

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.deeplearningsample.device.CameraHandlerThread
import java.io.IOException


class CameraPreview(
    context: Context,
    private val mCamera: Camera,
    private val mCameraHandlerThread: CameraHandlerThread
) : SurfaceView(context), SurfaceHolder.Callback, Camera.PreviewCallback {
    private val TAG = "[CameraPreview]"

    private val mHolder: SurfaceHolder = holder.apply {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraPreview)
        // deprecated setting, but required on Android versions prior to 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera.apply {
            try {
                setupCameraPreview()
                startCameraPreview()
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: ${e.message}")
            }
        }
    }

    private fun setupCameraPreview() {
        mCamera.setDisplayOrientation(90)
        val parameters = mCamera.parameters
        parameters.previewFrameRate = 30
        mCamera.setPreviewCallback(this)
        mCamera.setPreviewDisplay(holder)
        mCamera.parameters = parameters
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startCameraPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        Log.w(TAG, "onPreviewFrame")
    }

    fun takePicture(callback: Camera.PictureCallback) {
        mCamera.takePicture(null, null, callback)
    }

    fun release() {
        mCamera.stopPreview()
        mCamera.setPreviewCallback(null)
        mCamera.release()
    }

    fun startCameraPreview() {
        mCameraHandlerThread.startPreview(mCamera)
    }
}