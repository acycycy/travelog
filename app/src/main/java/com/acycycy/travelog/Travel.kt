package com.acycycy.travelog

data class Travel(
    val id: Int,
    val title: String,
    val date: String,
    val memo: String,
    val imageUri: String?,
    val rating: Int = 0,
    val location: String? = null  // "lat,lng" 형식
)
