package com.example.riskapp.models

import com.google.firebase.Timestamp

data class Job(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val risks: List<String> = emptyList(),
    val extraRisks: String = "",
    val createdBy: Map<String, String> = emptyMap(), // { uid: "", name: "", email: "" }
    val assignedTo: Map<String, String> = emptyMap(), // same shape
    val status: String = "open",
    val createdAt: Timestamp? = null,
    val validity: Timestamp? = null,
    val startedAt: Timestamp? = null,
    val closedAt: Timestamp? = null
)
