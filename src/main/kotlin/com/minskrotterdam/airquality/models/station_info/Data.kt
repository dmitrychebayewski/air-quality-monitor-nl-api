package com.minskrotterdam.airquality.models.station_info

data class Data(
    val components: List<String>,
    val description: Description,
    val geometry: Geometry,
    val location: String,
    val municipality: Any?,
    val organisation: String,
    val province: String,
    val type: String,
    val url: String,
    val year_start: String
)