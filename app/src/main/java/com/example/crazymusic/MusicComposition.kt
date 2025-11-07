package com.example.crazymusic

import java.util.*

data class MusicComposition(
    var name: String,
    var duration: Long,
    val id: String = UUID.randomUUID().toString(),
    val markers: MutableList<VegetableMarker> = mutableListOf(),
    val creationDate: Date = Date()
) {
    fun addMarker(marker: VegetableMarker) {
        markers.add(marker)
    }
}