package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

// Model untuk item di dalam list
data class MaterialListItem(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("order")
    val order: Int,
    @SerializedName("createdAt")
    val createdAt: String
)

// Model untuk detail materi
data class MaterialDetail(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("order")
    val order: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("module")
    val module: ModuleInfo
)

data class ModuleInfo(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String
)
