package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.config.LUCHTMEET_URL_ENDPOINT
import com.minskrotterdam.airquality.models.measurements.PagedMeasurements
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.Duration


class MeasurementsService {

    fun getMeasurement(stationId: List<String>, formula: String, startTime: String, endTime: String, page: Int): Call<PagedMeasurements> {
        return Api.create().getMeasurements(startTime,
                endTime, page, formula, *stationId.toTypedArray())
    }

    private interface Api {

        @GET("measurements")
        fun getMeasurements(
                @Query("start") start: String,
                @Query("end") end: String,
                @Query("page") page: Int,
                @Query("formula") formula: String,
                @Query("station_number") vararg stationNumber: String): Call<PagedMeasurements>

        companion object Factory {
            fun create(): Api {
                val retrofit = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(80)).callTimeout(Duration.ofSeconds(80)).readTimeout(Duration.ofSeconds(80)).build())
                        .baseUrl(LUCHTMEET_URL_ENDPOINT)
                        .build()

                return retrofit.create(Api::class.java)
            }
        }
    }
}