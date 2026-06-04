package com.dwipayana.literalearn.data.csv

import android.content.Context
import com.dwipayana.literalearn.data.model.Recommendation
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvReader(private val context: Context) {
    fun readDataset(): List<Recommendation> {
        val dataset = mutableListOf<Recommendation>()
        try {
            val inputStream = context.assets.open("dataset_ml_1000.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip Header
            reader.readLine() 
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line?.split(",") ?: continue
                if (tokens.size >= 6) {
                    dataset.add(
                        Recommendation(
                            score = tokens[0].trim().toInt(),
                            topic = tokens[1].trim(),
                            difficulty = tokens[2].trim(),
                            wrongAnswers = tokens[3].trim().toInt(),
                            timeSpent = tokens[4].trim().toInt(),
                            recommendedVideo = tokens[5].trim()
                        )
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataset
    }
}
