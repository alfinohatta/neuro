package com.example.neuro.data.api

import com.example.neuro.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface NeuroApiService {

    // --- Users & Profile ---
    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): Response<User>

    @GET("users/{userId}/preferences")
    suspend fun getUserPreferences(@Path("userId") userId: Long): Response<UserPreferences>

    @PUT("users/{userId}/preferences")
    suspend fun updateUserPreferences(
        @Path("userId") userId: Long,
        @Body preferences: UserPreferences
    ): Response<UserPreferences>

    // --- Consents ---
    @GET("users/{userId}/consents")
    suspend fun getUserConsents(@Path("userId") userId: Long): Response<List<UserConsent>>

    @POST("users/{userId}/consents")
    suspend fun updateConsent(
        @Path("userId") userId: Long,
        @Body consent: UserConsent
    ): Response<UserConsent>

    // --- Focus Sessions ---
    @GET("users/{userId}/sessions")
    suspend fun getUserSessions(@Path("userId") userId: Long): Response<List<FocusSession>>

    @POST("sessions")
    suspend fun createSession(@Body session: FocusSession): Response<FocusSession>

    @PUT("sessions/{id}")
    suspend fun updateSession(
        @Path("id") sessionId: Long,
        @Body session: FocusSession
    ): Response<FocusSession>

    @POST("sessions/{id}/end")
    suspend fun endSession(@Path("id") sessionId: Long): Response<FocusSession>

    // --- Signal Measurements ---
    @POST("signals")
    suspend fun postSignalMeasurement(@Body measurement: SignalMeasurement): Response<SignalMeasurement>

    @GET("sessions/{sessionId}/signals")
    suspend fun getSessionSignals(@Path("sessionId") sessionId: Long): Response<List<SignalMeasurement>>

    // --- Recommendations ---
    @GET("sessions/{sessionId}/recommendations")
    suspend fun getSessionRecommendations(@Path("sessionId") sessionId: Long): Response<List<Recommendation>>

    @PUT("recommendations/{id}/status")
    suspend fun updateRecommendationStatus(
        @Path("id") id: Long,
        @Query("status") status: RecommendationStatus
    ): Response<Recommendation>

    // --- Feedback & Summaries ---
    @POST("sessions/{sessionId}/feedback")
    suspend fun submitFeedback(
        @Path("sessionId") sessionId: Long,
        @Body feedback: SessionFeedback
    ): Response<SessionFeedback>

    @GET("sessions/{sessionId}/summary")
    suspend fun getSessionSummary(@Path("sessionId") sessionId: Long): Response<SessionSummary>
}
