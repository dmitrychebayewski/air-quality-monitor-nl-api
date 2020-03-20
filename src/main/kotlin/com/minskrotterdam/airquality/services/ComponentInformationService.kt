package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.common.LUCHTMEET_URL_ENDPOINT
import com.minskrotterdam.airquality.models.component_info.ComponentInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class ComponentInformationService {

    fun getPollutantInfo(formula: String): Call<ComponentInfo> {
        return Api.create().getPollutantInfo(formula)
    }

    private interface Api {

        @GET("components/{formula}")
        fun getPollutantInfo(
                @Path("formula") formula: String): Call<ComponentInfo>

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