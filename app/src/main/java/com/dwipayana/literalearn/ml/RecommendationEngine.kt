package com.dwipayana.literalearn.ml

import com.dwipayana.literalearn.data.model.QuizResult
import com.dwipayana.literalearn.data.model.Recommendation

class RecommendationEngine {
    fun findBestRecommendation(
        userResult: QuizResult,
        dataset: List<Recommendation>
    ): Recommendation? {
        // Logika: Filter berdasarkan topik yang sama
        val topicMatches = dataset.filter { it.topic.equals(userResult.topic, ignoreCase = true) }
        
        // Cari baris yang paling mendekati skor user dan jumlah salah jawaban
        return topicMatches.minByOrNull { row ->
            val scoreDiff = Math.abs(row.score - userResult.score)
            val wrongDiff = Math.abs(row.wrongAnswers - userResult.wrongAnswers)
            scoreDiff + wrongDiff // Manhattan distance sederhana
        }
    }
}
