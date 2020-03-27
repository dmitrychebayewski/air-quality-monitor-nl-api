package com.minskrotterdam.airquality.models.measurements

data class ExtData(
        val formula: String,
        val station_number: String,
        val timestamp_measured: String,
        var value: Double,
        val aggr: String,
        val coordinates: List<Double>
)