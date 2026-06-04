package com.dwipayana.literalearn.data.model

data class Recommendation(
    val score: Int,
    val topic: String,
    val difficulty: String,
    val wrongAnswers: Int,
    val timeSpent: Int,
    val recommendedVideo: String
)

data class QuizResult(
    val score: Int,
    val topic: String,
    val wrongAnswers: Int
)
