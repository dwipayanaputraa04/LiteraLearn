package com.dwipayana.literalearn.data.network

import com.dwipayana.literalearn.data.model.ApiResponse
import com.dwipayana.literalearn.data.model.AuthResponse
import com.dwipayana.literalearn.data.model.LoginRequest
import com.dwipayana.literalearn.data.model.ProgressResponse
import com.dwipayana.literalearn.data.model.RegisterRequest
import com.dwipayana.literalearn.data.model.SaveMaterialRequest
import com.dwipayana.literalearn.data.model.SavedMaterialItem
import com.dwipayana.literalearn.data.model.UpdateProfileRequest
import com.dwipayana.literalearn.data.model.UpdateProgressRequest
import com.dwipayana.literalearn.data.model.UpdateProgressResponse
import com.dwipayana.literalearn.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/signin")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @GET("users/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<User>>

    @PATCH("users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<User>>

    @GET("progress")
    suspend fun getUserProgress(
        @Query("userUuid") userUuid: String
    ): Response<ApiResponse<List<ProgressResponse>>>

    @PATCH("progress/{subjectUuid}")
    suspend fun updateSubjectProgress(
        @Path("subjectUuid") subjectUuid: String,
        @Body request: UpdateProgressRequest
    ): Response<ApiResponse<UpdateProgressResponse>>

    // --- Saved Materials Endpoints ---
    @POST("saved-materials")
    suspend fun saveMaterial(
        @Body request: SaveMaterialRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("saved-materials/{materialUuid}")
    suspend fun unsaveMaterial(
        @Path("materialUuid") materialUuid: String,
        @Query("userUuid") userUuid: String
    ): Response<ApiResponse<Unit>>

    @GET("saved-materials")
    suspend fun getSavedMaterials(
        @Query("userUuid") userUuid: String
    ): Response<ApiResponse<List<SavedMaterialItem>>>
}
