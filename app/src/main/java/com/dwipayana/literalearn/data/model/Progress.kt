package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class ProgressResponse(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("progressPercent")
    val progressPercent: Double,
    @SerializedName("subject")
    val subject: SubjectInfo
)

data class SubjectInfo(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String
)

data class UpdateProgressRequest(
    @SerializedName("userUuid")
    val userUuid: String,
    @SerializedName("completionPercentage")
    val completionPercentage: Int
)

data class UpdateProgressResponse(
    @SerializedName("completionPercentage")
    val completionPercentage: Int,
    @SerializedName("updatedAt")
    val updatedAt: String
)
