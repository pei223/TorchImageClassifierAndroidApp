package com.example.deeplearningsample.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat

interface OnCameraObtainedListener {
    fun onCameraObtained(camera: Camera?)
    fun onError()
}

class CameraHandlerThread : HandlerThread("CameraHandlerThread") {
    private var mHandler: Handler

    init {
        start()
        mHandler = Handler(looper)
    }

    fun openCamera(callback: OnCameraObtainedListener) {
        mHandler.post(Runnable {
            try {
                val camera = Camera.open()
                callback.onCameraObtained(camera)
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onError()
            }
        })
    }

    fun post(runnable: Runnable) {
        mHandler.post(runnable)
    }

    fun startPreview(camera: Camera) {
        mHandler.post(Runnable {
            camera.startPreview()
        })
    }
}

class CameraWrapper {
    private val mHandlerTread: CameraHandlerThread = CameraHandlerThread()

    fun runOnCameraHandlerThread(runnable: Runnable) {
        mHandlerTread.post(runnable)
    }

    fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun checkPermission(context: Context): Boolean {
        val cameraPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    fun getCameraInstance(callback: OnCameraObtainedListener) {
        mHandlerTread.openCamera(callback)
    }

    fun startPreview() {

    }
}

class CameraDeviceUtil {
    fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun checkPermission(context: Context): Boolean {
        val cameraPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    fun getCameraInstance(callback: OnCameraObtainedListener) {
        CameraHandlerThread().openCamera(callback)
    }
}