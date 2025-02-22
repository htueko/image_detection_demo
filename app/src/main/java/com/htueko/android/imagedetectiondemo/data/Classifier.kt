package com.htueko.android.imagedetectiondemo.data

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.ArrayList


class Classifier(assetManager: AssetManager) {

    private val labels: List<String>
    private val model: Interpreter
//    private val inputSize = 0
//    private val IMAGE_MEAN = 128
//    private val IMAGE_STD = 128.0f
//    private val MAX_RESULTS = 3
//    private val BATCH_SIZE = 1
//    private val PIXEL_SIZE = 3
//    private val THRESHOLD = 0.1f

    init {
        model = Interpreter(getModelByteBuffer(assetManager, MODEL_PATH))
        labels = getLabels(assetManager, LABELS_PATH)
    }

    fun recognize(data: ByteArray): List<Recognition> {
        val result = Array(BATCH_SIZE) { FloatArray(labels.size) }

        val unscaledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmap =
            Bitmap.createScaledBitmap(unscaledBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, false)

        val byteBuffer = ByteBuffer
            .allocateDirect(
                BATCH_SIZE *
                        MODEL_INPUT_SIZE *
                        MODEL_INPUT_SIZE *
                        BYTES_PER_CHANNEL *
                        PIXEL_SIZE
            )
            .apply { order(ByteOrder.nativeOrder()) }

        val pixelValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        bitmap.getPixels(pixelValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until MODEL_INPUT_SIZE) {
            for (j in 0 until MODEL_INPUT_SIZE) {
                val pixelValue = pixelValues[pixel++]
                byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255f)
                byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255f)
                byteBuffer.putFloat((pixelValue and 0xFF) / 255f)
            }
        }
        // FIXME: 31/05/20 Cannot copy between a TensorFlowLite tensor with shape [1, 16] and a Java object with shape [1, 32]. 
        model.run(byteBuffer, result)
        return parseResults(result)
    }

    private fun parseResults(result: Array<FloatArray>): List<Recognition> {

        val recognitions = mutableListOf<Recognition>()

        labels.forEachIndexed { index, label ->
            val probability = result[0][index]
            recognitions.add(Recognition(label, probability))
        }

        return recognitions.sortedByDescending { it.probability }
    }

    @Throws(IOException::class)
    private fun getModelByteBuffer(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            .asReadOnlyBuffer()
    }

    @Throws(IOException::class)
    private fun getLabels(assetManager: AssetManager, labelPath: String): List<String> {
        val labels = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        while (true) {
            val label = reader.readLine() ?: break
            labels.add(label)
        }
        reader.close()
        return labels
    }

    companion object {
        private const val BATCH_SIZE = 1 // process only 1 image at a time
        private const val MODEL_INPUT_SIZE = 300//224 // 224x224
        private const val BYTES_PER_CHANNEL = 4 // float size
        private const val PIXEL_SIZE = 3 // rgb

        private const val LABELS_PATH = "rps_labels.txt"
        private const val MODEL_PATH = "rock_paper_sci_model.tflite"
    }

}