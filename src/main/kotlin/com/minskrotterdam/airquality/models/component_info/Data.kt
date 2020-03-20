package com.minskrotterdam.airquality.models.component_info

data class Data(
        val description: Description,
        val formula: String,
        val limits: List<Limit>,
        val name: Name
)