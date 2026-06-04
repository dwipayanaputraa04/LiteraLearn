package com.dwipayana.literalearn.data.model

import com.google.gson.annotations.SerializedName

data class SaveMaterialRequest(
    @SerializedName("userUuid")
    val userUuid: String,
    @SerializedName("materialUuid")
    val materialUuid: String
)

data class SavedMaterialItem(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("material")
    val material: MaterialListItemWithModule? // Make nullable for safety
)

data class MaterialListItemWithModule(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("module")
    val module: ModuleSummary? // Make nullable in case backend doesn't 'include' it
)

data class ModuleSummary(
    @SerializedName("title")
    val title: String
)
