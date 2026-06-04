package com.dwipayana.literalearn.data.network

import com.dwipayana.literalearn.data.model.ApiResponse
import com.dwipayana.literalearn.data.model.MaterialDetail
import com.dwipayana.literalearn.data.model.MaterialListItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MaterialApiService {
    
    @GET("materials")
    suspend fun getMaterialsByModule(
        @Query("moduleUuid") moduleUuid: String
    ): Response<ApiResponse<List<MaterialListItem>>>

    @GET("materials/{uuid}")
    suspend fun getMaterialDetail(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<MaterialDetail>>
}
