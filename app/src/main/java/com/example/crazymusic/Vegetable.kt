package com.example.crazymusic

data class Vegetable(
    val name: String,
    val drawableId: Int,
    val soundId: Int,
    val modelId: String,
    var isActive: Boolean = true
)