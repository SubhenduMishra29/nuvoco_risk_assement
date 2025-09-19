package com.example.riskapp.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "worker", // worker | manager | safety | admin
    val isManager: Boolean = false
)
