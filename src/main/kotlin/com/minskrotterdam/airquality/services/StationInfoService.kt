package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.config.LUCHTMEET_URL_ENDPOINT
import com.minskrotterdam.airquality.models.station_info.StationInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class StationInfoService {

    fun getStationDetails(stationNumber: String): Call<StationInfo> {
        return Api.create().getStationInfoDetails(stationNumber)
    }

    private interface Api {

        @GET("stations/{station_number}")
        fun getStationInfoDetails(
                @Path("station_number") stationNumber: String): Call<StationInfo>

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