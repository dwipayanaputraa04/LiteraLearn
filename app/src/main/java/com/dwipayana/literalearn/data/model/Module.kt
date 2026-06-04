package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class Module(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("subject_id")
    val subjectId: Int? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("order")
    val order: Int,
    @SerializedName("materials")
    val materials: List<MaterialListItem>? = null,
    
    // Pastikan ini adalah List agar cocok dengan array dari backend
    @SerializedName("quizzes")
    val quizzes: List<QuizInfo> = emptyList(),
    
    @SerializedName("quiz") // Fallback objek tunggal
    val quiz: QuizInfo? = null,
    
    @SerializedName("quizUuid") // Fallback string langsung
    val quizUuid: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class QuizInfo(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("minScore")
    val minScore: Int? = null
)

data class CreateModuleRequest(
    @SerializedName("subjectUuid")
    val subjectUuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("order")
    val order: Int
)

data class UpdateModuleRequest(
    @SerializedName("subjectUuid")
    val subjectUuid: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("order")
    val order: Int? = null
)
