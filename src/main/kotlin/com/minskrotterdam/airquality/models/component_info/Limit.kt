package com.minskrotterdam.airquality.models.component_info

data class Limit(
        val color: String,
        val lowerband: Int?,
        val rating: Int,
        val type: String,
        val upperband: Int
)