package com.minskrotterdam.airquality.models.stations

data class Pagination(
        val current_page: Int,
        val first_page: Int,
        val last_page: Int,
        val next_page: Int,
        val page_list: List<Int>,
        val prev_page: Int
)