package com.dwipayana.literalearn.data.network

import com.dwipayana.literalearn.data.model.ApiResponse
import com.dwipayana.literalearn.data.model.CreateSubjectRequest
import com.dwipayana.literalearn.data.model.Subject
import com.dwipayana.literalearn.data.model.UpdateSubjectRequest
import retrofit2.Response
import retrofit2.http.*

interface SubjectApiService {

    @GET("subjects")
    suspend fun getAllSubjects(
        @Query("grade") grade: Int? = null
    ): Response<ApiResponse<List<Subject>>>

    @GET("subjects/{uuid}")
    suspend fun getSubjectByUuid(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<Subject>>

    @POST("subjects")
    suspend fun createSubject(
        @Body request: CreateSubjectRequest
    ): Response<ApiResponse<Subject>>

    @PATCH("subjects/{uuid}")
    suspend fun updateSubject(
        @Path("uuid") uuid: String,
        @Body request: UpdateSubjectRequest
    ): Response<ApiResponse<Subject>>

    @DELETE("subjects/{uuid}")
    suspend fun deleteSubject(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<Unit>>
}
