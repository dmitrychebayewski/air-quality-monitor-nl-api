package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.common.LUCHTMEET_URL_ENDPOINT
import com.minskrotterdam.airquality.models.components.PollutantComponents
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class ComponentsService {

    fun getPollutants(page: Int): Call<PollutantComponents> {
        return Api.create().getPollutants(page)
    }

    private interface Api {

        @GET("components")
        fun getPollutants(
                @Query("page") page: Int): Call<PollutantComponents>

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