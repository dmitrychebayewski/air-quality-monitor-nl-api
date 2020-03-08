package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.common.LUCHTMEET_URL_ENDPOINT
import com.minskrotterdam.airquality.models.stations.Stations
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class StationsService {

    fun getStations(page: Int): Call<Stations> {
        return Api.create().getStations(page)
    }

    private interface Api {

        @GET("stations")
        fun getStations(
                @Query("page") page: Int): Call<Stations>

        companion object Factory {
            fun create(): Api {
                val retrofit = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(LUCHTMEET_URL_ENDPOINT)
                        .build()
                return retrofit.create(Api::class.java)
            }
        }
    }
}