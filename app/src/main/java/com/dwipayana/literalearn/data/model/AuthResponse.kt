package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: AccessTokenData
)

data class AccessTokenData(
    @SerializedName("access_token")
    val token: String
)
