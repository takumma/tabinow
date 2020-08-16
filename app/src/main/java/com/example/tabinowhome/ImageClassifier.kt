package com.example.tflitesample

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.example.tflitesample.Keys.DIM_BATCH_SIZE
import com.example.tflitesample.Keys.DIM_IMG_SIZE_X
import com.example.tflitesample.Keys.DIM_IMG_SIZE_Y
import com.example.tflitesample.Keys.DIM_PIXEL_SIZE
import com.example.tflitesample.Keys.INPUT_SIZE
import com.example.tflitesample.Keys.LABEL_PATH
import com.example.tflitesample.Keys.MODEL_PATH
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

object Keys {
    const val MODEL_PATH = "models_edge_ICN3951207797555600647_2019-10-22_05-24-20-897_tflite_model.tflite"
    const val LABEL_PATH = "models_edge_ICN3951207797555600647_2019-10-22_05-24-20-897_tflite_dict.txt"
    const val INPUT_SIZE = 224
    const val DIM_BATCH_SIZE = 1
    const val DIM_PIXEL_SIZE = 3
    const val DIM_IMG_SIZE_X = 224
    const val DIM_IMG_SIZE_Y = 224
}

class ImageClassifier constructor(private val assetManager: AssetManager) {

    private var interpreter: Interpreter? = null
    private var labelProb: Array<ByteArray>
    private val labels = Vector<String>()
    private val intValues by lazy { IntArray(INPUT_SIZE * INPUT_SIZE) }
    private var imgData: ByteBuffer

    init {
        try {
            val br = BufferedReader(InputStreamReader(assetManager.open(LABEL_PATH)))
            while (true) {
                val line = br.readLine() ?: break
                labels.add(line)
            }
            br.close()
        } catch (e: IOException) {
            throw RuntimeException("Problem reading label file!", e)
        }
        labelProb = Array(1) { ByteArray(labels.size) }
        imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData!!.order(ByteOrder.nativeOrder())
        try {
            interpreter = Interpreter(loadModelFile(assetManager, MODEL_PATH))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if (imgData == null) return
        imgData!!.rewind()
        Log.d("debug width",bitmap.width.toString())
        Log.d("debug height",bitmap.height.toString())
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues!![pixel++]
                imgData!!.put((value shr 16 and 0xFF).toByte())
                imgData!!.put((value shr 8 and 0xFF).toByte())
                imgData!!.put((value and 0xFF).toByte())
            }
        }
    }

    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun recognizeImage(bitmap:Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE,false)
        convertBitmapToByteBuffer(scaledBitmap)
        interpreter!!.run(imgData, labelProb)

        var maxindex = 0
        for (i in 1 .. labels.size-1){
            if (labelProb[0][maxindex]<labelProb[0][i]){
                maxindex = i
            }
        }
        return labels[maxindex]
    }

    fun close() {
        interpreter?.close()
    }
}