package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class Subject(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("grade")
    val grade: Int,
    @SerializedName("accent_color")
    val accentColor: String? = null,
    @SerializedName("container_color")
    val containerColor: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("modules")
    val modules: List<Module>? = null,
    var currentProgress: Double = 0.0 // Tambahkan ini untuk UI
)

data class CreateSubjectRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("grade")
    val grade: Int
)

data class UpdateSubjectRequest(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("grade")
    val grade: Int? = null
)
