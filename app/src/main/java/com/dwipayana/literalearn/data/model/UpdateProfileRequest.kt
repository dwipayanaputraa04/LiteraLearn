package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("grade")
    val grade: Int? = null,
    @SerializedName("profile")
    val profile: String? = null // Base64 string
)
