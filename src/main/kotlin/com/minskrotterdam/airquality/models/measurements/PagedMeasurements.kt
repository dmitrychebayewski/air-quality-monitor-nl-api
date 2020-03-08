package com.minskrotterdam.airquality.models.measurements

data class PagedMeasurements(
        val `data`: List<Data>,
        val pagination: Pagination
)