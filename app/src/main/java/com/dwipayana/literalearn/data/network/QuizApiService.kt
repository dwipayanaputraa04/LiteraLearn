package com.dwipayana.literalearn.data.network

import com.dwipayana.literalearn.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface QuizApiService {

    @GET("quizzes/{uuid}")
    suspend fun getQuizDetail(
        @Path("uuid") uuid: String
    ): Response<ApiResponse<QuizDetailResponse>>

    @POST("quiz-attempts")
    suspend fun createAttempt(
        @Body request: CreateAttemptRequest
    ): Response<ApiResponse<AttemptResponse>>

    @POST("quiz-attempts/{attemptUuid}/answers")
    suspend fun submitAnswer(
        @Path("attemptUuid") attemptUuid: String,
        @Body request: SubmitAnswerRequest
    ): Response<ApiResponse<SubmitAnswerResponse>>

    @POST("quiz-attempts/{attemptUuid}/submit")
    suspend fun submitQuiz(
        @Path("attemptUuid") attemptUuid: String
    ): Response<ApiResponse<QuizResultResponse>>

    @GET("questions")
    suspend fun getQuestionsByQuiz(
        @Query("quizUuid") quizUuid: String
    ): Response<ApiResponse<List<QuestionItem>>>
}
