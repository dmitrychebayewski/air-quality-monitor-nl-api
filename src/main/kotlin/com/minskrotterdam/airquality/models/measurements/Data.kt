package com.minskrotterdam.airquality.models.measurements

data class Data(
        val formula: String,
        val station_number: String,
        val timestamp_measured: String,
        val value: Double
)