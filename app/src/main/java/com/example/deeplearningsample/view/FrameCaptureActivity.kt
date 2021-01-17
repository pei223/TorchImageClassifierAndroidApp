package com.example.deeplearningsample.view

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.deeplearningsample.R
import com.example.deeplearningsample.device.CameraDeviceUtil
import com.example.deeplearningsample.device.CameraHandlerThread
import com.example.deeplearningsample.device.OnCameraObtainedListener
import com.example.deeplearningsample.model.ABNImageClassifier
import com.example.deeplearningsample.model.BaseImageClassifier
import com.example.deeplearningsample.model.SimpleImageClassifier
import com.example.deeplearningsample.model.STL10Class


class FrameCaptureActivity : BasePreviewActivity() {
    private val TAG = "[FrameCaptureActivity]"
    private var mPreview: CameraPreview? = null
    private var mBusy = false

    private lateinit var mUIHandler: Handler

    private lateinit var mClassifier: BaseImageClassifier

    private val mOnPreviewFrame = object : FrameCallback {
        override fun onPreviewFrame(yuvData: ByteArray?) {
            if (mBusy) {
                Log.d(TAG, "Busy.")
                return
            }
            if (yuvData == null) {
                Log.w(TAG, "Frame is null.")
                return
            }
            if (yuvData.isEmpty()) {
                Log.w(TAG, "Frame is empty.")
                return
            }
            mPreview?.let { it ->
                mBusy = true
                val start: Double = System.currentTimeMillis().toDouble()
                val imageBitmap: Bitmap = it.convertJpegBitmapFromYUV(yuvData)
                val resultClass: STL10Class = mClassifier.classify(imageBitmap)
                mClassifier.classify(imageBitmap)
                mBusy = false
                mUIHandler.post(Runnable {
                    showResult(resultClass, System.currentTimeMillis() / start)
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUIHandler = Handler(this.mainLooper)
        setContentView(R.layout.activity_frame_capture)
//        mClassifier = SimpleImageClassifier(this, 256, 256, "mobilenet_model_android.pt")
        mClassifier = ABNImageClassifier(this, 96, (96 * 1.5).toInt(), "abn_model_android.pt")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        setContentView(R.layout.activity_frame_capture)
        setupToolbar()
        val cameraUtil = CameraDeviceUtil()
        if (!cameraUtil.checkCameraHardware(this)) {
            setCameraDisableView()
            Log.d(TAG, "Camera device is disabled.")
            return
        }
        if (!cameraUtil.checkPermission(this)) {
            requestCameraPermission()
            return
        }
        setCameraPreview()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        mPreview?.release()
        mPreview = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setCameraPreview() {
        val cameraHandlerThread = CameraHandlerThread()
        cameraHandlerThread.openCamera(callback = object : OnCameraObtainedListener {
            override fun onCameraObtained(camera: Camera?) {
                if (camera == null) {
                    Log.e(TAG, "Camera is null")
                    return
                }
                mUIHandler.post(Runnable {
                    setCameraPreviewAfterCameraObtained(camera, cameraHandlerThread)
                })
            }

            override fun onError() {
            }
        })
    }

    private fun setCameraPreviewAfterCameraObtained(
        camera: Camera,
        cameraHandlerThread: CameraHandlerThread
    ) {
        mPreview = CameraPreview(this, camera, cameraHandlerThread)

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it, 0)
            it.setFrameCallback(mOnPreviewFrame)
        }

        val disableView: View = findViewById(R.id.disable_view)
        disableView.visibility = View.GONE

        mPreview?.startCameraPreview()
    }

    private fun setCameraDisableView() {
        val disableView: View = findViewById(R.id.disable_view)
        disableView.visibility = View.VISIBLE
    }

    private fun requestCameraPermission() {
        val cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.CAMERA
        )

        // If 'not display again' selected.
        if (cameraRationale) {
            AlertDialog.Builder(this)
                .setMessage("デバイスの「設定」でカメラの権限を許可してください。")
                .setPositiveButton("OK") { dialog, _ ->
                    showAppPermissionSetting()
                    dialog.dismiss()
                }
                .create().show()
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            0
        )
    }

    private fun showAppPermissionSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + this.packageName)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun showResult(result_class: STL10Class, inferenceTimeMs: Double) {
        val resultView: TextView = findViewById(R.id.result_view)
        resultView.visibility = View.VISIBLE
        resultView.text = result_class.className
        val inferenceTimeView: TextView = findViewById(R.id.inference_time)
        inferenceTimeView.text = (inferenceTimeMs / 1000.0).toString() + " ms"
    }
}