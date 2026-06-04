package com.dwipayana.literalearn.ml

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class VideoRecommendationHelper(context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    companion object {
        private const val MODEL_PATH = "model_rekomendasi.tflite"
        private const val LABELS_PATH = "labels.txt"
        private const val TAG = "TFLiteRecommendation"
    }

    init {
        try {
            // 1. Load Model Manually to avoid library issues
            val modelBuffer = loadModelFile(context.assets, MODEL_PATH)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)

            // 2. Load Labels Manually
            labels = loadLabelList(context, LABELS_PATH)
            
            Log.d(TAG, "Model and Labels loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model or labels: ${e.localizedMessage}")
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(context: Context, labelPath: String): List<String> {
        return context.assets.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    /**
     * Prediksi modul terbaik berdasarkan 8 nilai kuis.
     * @param scores Array berisi 8 nilai kuis (Float)
     * @return Nama modul (label) dengan probabilitas tertinggi
     */
    fun predictModule(scores: FloatArray): String {
        val currentInterpreter = interpreter
        if (currentInterpreter == null || labels.isEmpty()) {
            return "Helper not initialized"
        }

        if (scores.size != 8) {
            return "Invalid input size: Expected 8 scores"
        }

        try {
            // Input: [1, 8] - 1 batch, 8 features
            val inputBuffer = ByteBuffer.allocateDirect(1 * 8 * 4) // 4 bytes per float
            inputBuffer.order(ByteOrder.nativeOrder())
            for (score in scores) {
                inputBuffer.putFloat(score)
            }
            inputBuffer.rewind()

            // Output: [1, num_labels]
            val outputArray = Array(1) { FloatArray(labels.size) }

            // Run Inference
            currentInterpreter.run(inputBuffer, outputArray)

            // Find Argmax
            val probabilities = outputArray[0]
            var maxIndex = 0
            var maxProb = -1f

            for (i in probabilities.indices) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i]
                    maxIndex = i
                }
            }

            return if (maxIndex < labels.size) {
                labels[maxIndex]
            } else {
                "Unknown Label"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during inference: ${e.localizedMessage}")
            return "Prediction Error"
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
