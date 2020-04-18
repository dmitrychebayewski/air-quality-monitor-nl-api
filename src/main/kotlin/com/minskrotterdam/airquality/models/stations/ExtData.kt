package com.minskrotterdam.airquality.models.stations

data class ExtData(
        val number: String,
        val location: String,
        val municipality: Any?,
        val coordinates: Coordinates,
        val components: List<String>
)