package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

// Response Models
data class QuizDetailResponse(
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("minScore")
    val minScore: Int? = null,
    @SerializedName("module")
    val module: ModuleInfo? = null,
    @SerializedName("questions")
    val questions: List<QuestionItem> = emptyList()
)

data class QuestionItem(
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("options")
    val options: List<OptionItem> = emptyList()
)

data class OptionItem(
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("text")
    val text: String? = null
)

data class AttemptResponse(
    @SerializedName("uuid") // Seringkali backend menggunakan 'uuid'
    val uuid: String? = null,
    @SerializedName("attemptUuid") // Sesuai panduan
    val attemptUuid: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class QuizResultResponse(
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("score")
    val score: Int? = null,
    @SerializedName("isPassed")
    val isPassed: Boolean? = null
)

// Request Models
data class CreateAttemptRequest(
    @SerializedName("userUuid")
    val userUuid: String,
    @SerializedName("quizUuid")
    val quizUuid: String
)

data class SubmitAnswerRequest(
    @SerializedName("questionUuid")
    val questionUuid: String,
    @SerializedName("chosenOptionLabel")
    val chosenOptionLabel: String
)

data class SubmitAnswerResponse(
    @SerializedName("isCorrect")
    val isCorrect: Boolean? = null,
    @SerializedName("correctOptionLabel")
    val correctOptionLabel: String? = null
)
