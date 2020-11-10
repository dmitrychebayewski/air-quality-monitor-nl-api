package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.safeLaunch
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import io.vertx.kotlin.core.http.writeAwait

class AggregatedComponentsMeasurementHandler(vertx: Vertx) : BaseAggregatedComponentsMeasurementHandler(vertx) {

    suspend fun aggregatedComponentsMeasurementHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedComponentsMeasurement(ctx)
        }
    }

    private suspend fun getAggregatedComponentsMeasurement(ctx: RoutingContext) {
        val response = ctx.response()
        try {
            response.isChunked = true
            var chunk = top7()["results"]
            if (chunk == null) {
                chunk = get(ctx.request().params())
            }
            response.writeAwait(chunk)
        } finally {
            if (!response.ended()) {
                response.endAwait()
            }
        }
    }
}