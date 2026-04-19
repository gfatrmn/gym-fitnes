package com.example.arenafitness.network

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int?,
    val name: String?,
    val login: String?,
    val email: String?,
    val phone: String?,
    val role: String?,
    @SerializedName("birth_date") val birthDate: String?,
    val gender: String?,
    val address: String?,
    @SerializedName("is_member") val isMember: Int?
)

data class LoginResponse(
    val status: String?,
    val message: String?,
    @SerializedName("data") val user: User?
)

data class RegisterResponse(
    val status: String?,
    val message: String?,
    @SerializedName("data") val user: User? = null
)

data class UserResponse(
    val status: String?,
    val message: String?,
    @SerializedName("data") val user: User?
)

data class GymMember(
    val id: Int?,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    val phone: String?,
    @SerializedName("checkin_code") val checkinCode: String?,
    @SerializedName("member_status") val memberStatus: String?,
    @SerializedName("membership_plan") val membershipPlan: String?,
    @SerializedName("expires_at") val expiresAt: String?
)

data class Announcement(
    val id: Int?,
    val title: String?,
    val body: String?,
    @SerializedName("created_at") val createdAt: String?
)
