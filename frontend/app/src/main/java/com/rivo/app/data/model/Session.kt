package com.rivo.app.data.model

data class Session(
    val email: String,
    val isLoggedIn: Boolean,
    val userType: UserType,
    val token: String,
    val userId: String

)
