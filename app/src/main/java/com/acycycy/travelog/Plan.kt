package com.acycycy.travelog

data class Plan(
    val id: Int,
    val destination: String,
    val plannedDate: String,
    val memo: String,
    val location: String? = null
)
