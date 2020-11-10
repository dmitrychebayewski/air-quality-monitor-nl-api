package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.safeLaunch
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import io.vertx.kotlin.core.http.writeAwait

class AggregatedMeasurementsHandler(vertx: Vertx) : BaseAggregatedMeasurementsHandler(vertx) {

    suspend fun airMeasurementsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedMeasurements(ctx)
        }
    }

    suspend fun aggregatedAirMeasurementsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedMeasurements(ctx)
        }
    }

    private suspend fun getAggregatedMeasurements(ctx: RoutingContext) {
        val response = ctx.response()
        response.isChunked = true
        try {
            val requestParameters = ctx.request().params()
            val regio = ctx.pathParam("region")
            val stationId = ctx.pathParam("station_number")
            val formula = extractFormula(requestParameters)
            var result: String?
            if(!regio.isNullOrEmpty()) {
                result = regio(regio)[formula]
            } else {
                result =  get(requestParameters, regio, stationId)
            }
            if(result.isNullOrEmpty()) {
                result =  get(requestParameters, regio, stationId)
            }
            response.writeAwait(result)
        } finally {
            if (!response.ended()) {
                response.endAwait()
            }
        }
    }
}