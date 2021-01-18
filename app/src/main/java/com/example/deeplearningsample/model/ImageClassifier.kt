package com.example.deeplearningsample.model

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

abstract class BaseImageClassifier(
    context: Context,
    width: Int,
    height: Int,
    modelName: String
) {
    private val mWidth = width
    private val mHeight = height
    protected var mModule: Module

    init {
        val file = File(context.filesDir, modelName)
        if (!file.exists() || file.length() == 0L) {
            context.assets.open(modelName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
            }
        }
        mModule = Module.load(file.absolutePath)
    }

    protected fun bitmapToTensor(imageBitmap: Bitmap): Tensor {
        val resizedImageBitmap = Bitmap.createScaledBitmap(imageBitmap, mWidth, mHeight, true)
        return TensorImageUtils.bitmapToFloat32Tensor(
            resizedImageBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )
    }

    fun centerCropBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.width == bitmap.height) {
            return bitmap
        }
        if (bitmap.width > bitmap.height) {
            val leftOffset = (bitmap.width - bitmap.height) / 2
            return Bitmap.createBitmap(
                bitmap,
                leftOffset,
                0,
                bitmap.height,
                bitmap.height,
                null,
                true
            )
        }
        val topOffset = (bitmap.height - bitmap.width) / 2
        return Bitmap.createBitmap(
            bitmap,
            0,
            topOffset,
            bitmap.width,
            bitmap.width,
            null,
            true
        )
    }

    abstract fun classify(imageBitmap: Bitmap): Int
}


class SimpleImageClassifier(context: Context, width: Int, height: Int, modelName: String) :
    BaseImageClassifier(context, width, height, modelName) {
    override fun classify(imageBitmap: Bitmap): Int {
        val inputTensor: Tensor = this.bitmapToTensor(imageBitmap)

        val resultIValue = mModule.forward(IValue.from(inputTensor))

        val outputTensor: Tensor = resultIValue.toTensor()
        val scores: FloatArray = outputTensor.dataAsFloatArray

        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }
        return maxScoreIdx
    }
}

class ABNImageClassifier(context: Context, width: Int, height: Int, modelName: String) :
    BaseImageClassifier(context, width, height, modelName) {
    override fun classify(imageBitmap: Bitmap): Int {
        val inputTensor: Tensor = this.bitmapToTensor(imageBitmap)

        val resultIValue = mModule.forward(IValue.from(inputTensor))

        val outputTensor: Tensor = resultIValue.toTuple()[0].toTensor()
        val scores: FloatArray = outputTensor.dataAsFloatArray

        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in scores.indices) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }
        return maxScoreIdx
    }
}