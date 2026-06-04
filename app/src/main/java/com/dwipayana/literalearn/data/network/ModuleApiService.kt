package com.dwipayana.literalearn.data.network

import com.dwipayana.literalearn.data.model.ApiResponse
import com.dwipayana.literalearn.data.model.CreateModuleRequest
import com.dwipayana.literalearn.data.model.Module
import com.dwipayana.literalearn.data.model.UpdateModuleRequest
import retrofit2.Response
import retrofit2.http.*

interface ModuleApiService {

    @GET("modules")
    suspend fun getAllModules(
        @Query("subjectUuid") subjectUuid: String? = null
    ): Response<ApiResponse<List<Module>>>

    @GET("modules/{uuid}")
    suspend fun getModuleByUuid(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<Module>>

    @POST("modules")
    suspend fun createModule(
        @Body request: CreateModuleRequest
    ): Response<ApiResponse<Module>>

    @PATCH("modules/{uuid}")
    suspend fun updateModule(
        @Path("uuid") uuid: String,
        @Body request: UpdateModuleRequest
    ): Response<ApiResponse<Module>>

    @DELETE("modules/{uuid}")
    suspend fun deleteModule(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<Unit>>
}
