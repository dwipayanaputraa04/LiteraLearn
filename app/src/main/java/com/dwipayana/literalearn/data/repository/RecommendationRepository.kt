package com.dwipayana.literalearn.data.repository

import com.dwipayana.literalearn.data.csv.CsvReader
import com.dwipayana.literalearn.data.model.QuizResult
import com.dwipayana.literalearn.data.model.Recommendation
import com.dwipayana.literalearn.ml.RecommendationEngine

class RecommendationRepository(
    private val csvReader: CsvReader,
    private val engine: RecommendationEngine
) {
    fun getRecommendationForUser(userResult: QuizResult): Recommendation? {
        val dataset = csvReader.readDataset()
        return engine.findBestRecommendation(userResult, dataset)
    }
}
