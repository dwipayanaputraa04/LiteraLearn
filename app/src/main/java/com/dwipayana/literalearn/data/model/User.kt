package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("grade")
    val grade: Int?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("profile")
    val profile: String?
)

