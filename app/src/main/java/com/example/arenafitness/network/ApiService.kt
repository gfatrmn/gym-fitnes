package com.example.arenafitness.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("full_name") fullName: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<RegisterResponse>

    @GET("members")
    suspend fun getMembers(): Response<List<User>>

    @Multipart
    @POST("members/{id}/subscribe")
    suspend fun subscribe(
        @Path("id") userId: Int,
        @Part("plan_id") planId: RequestBody,
        @Part("membership_plan") planName: RequestBody,
        @Part("payment_amount") amount: RequestBody,
        @Part("payment_method") paymentMethod: RequestBody,
        @Part("invoice") invoice: RequestBody,
        @Part("notes") notes: RequestBody?,
        @Part("payment_proof") paymentProof: MultipartBody.Part?
    ): Response<RegisterResponse>

    @GET("members/{id}")
    suspend fun getMemberStatus(@Path("id") userId: Int): Response<UserResponse>
}
