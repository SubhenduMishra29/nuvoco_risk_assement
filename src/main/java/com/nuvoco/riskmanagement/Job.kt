package com.nuvoco.riskmanagement.models

data class Job(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "open",
    val createdBy: String = "",
    val assignedTo: String = "",
    val validity: String = ""
)
