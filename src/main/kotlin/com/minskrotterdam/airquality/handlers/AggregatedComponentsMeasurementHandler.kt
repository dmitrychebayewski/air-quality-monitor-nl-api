package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.config.HOURS_BACK
import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.models.AggregateBy
import com.minskrotterdam.airquality.models.ext.aggregateBy
import com.minskrotterdam.airquality.models.measurements.ExtData
import com.minskrotterdam.airquality.services.MeasurementsService
import com.minskrotterdam.airquality.services.StationInfoService
import io.vertx.core.MultiMap
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.gildor.coroutines.retrofit.await
import java.time.Instant
import java.time.temporal.ChronoUnit

class AggregatedComponentsMeasurementHandler {

    private fun extractAggregatorParam(requestParameters: MultiMap): AggregateBy {
        return when (requestParameters.get("aggr")) {
            AggregateBy.MAX.name.toLowerCase() -> AggregateBy.MAX
            AggregateBy.MIN.name.toLowerCase() -> AggregateBy.MIN
            else -> return AggregateBy.MAX
        }
    }

    private fun extractFormula(requestParameters: MultiMap?): Set<String> {
        val formula = requestParameters?.getAll("formula")
        if (formula.isNullOrEmpty()) {
            return emptySet<String>()
        }
        return setOf<String>(*formula.toTypedArray())
    }

    private fun extractEndTime(requestParameters: MultiMap): String {
        val endTime = requestParameters.get("end")
        if (endTime.isNullOrEmpty()) {
            return Instant.now().toString()
        }
        return endTime
    }

    private fun extractStartTime(requestParameters: MultiMap): String {
        val startTime = requestParameters.get("start")
        if (startTime.isNullOrEmpty()) {
            return Instant.now().minus(HOURS_BACK, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toString()
        }
        return startTime
    }

    suspend fun aggregatedComponentsMeasurementHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedComponentsMeasurement(ctx)
        }
    }

    private suspend fun getAggregatedComponentsMeasurement(ctx: RoutingContext) {
        val firstResult = mutableListOf<ExtData>()
        try {
            val response = ctx.response()
            response.isChunked = true

            val requestParameters = ctx.request().params()

            val stationId = emptyList<String>()

            val endTime = extractEndTime(requestParameters)
            val startTime = extractStartTime(requestParameters)

            val firstPage = MeasurementsService().getMeasurement(stationId, "", startTime, endTime, 1).await()
            val by = extractAggregatorParam(requestParameters)
            val filterByFormula = extractFormula(requestParameters)

            val groupBy = firstPage.data.groupBy { it.formula }
            val resultMap = if (filterByFormula.isEmpty()) groupBy else groupBy.filter { filterByFormula.contains(it.key) }

            val mutex = Mutex()
            resultMap.keys.map {
                CoroutineScope(Dispatchers.Default).async {
                    val aggregateBy = resultMap[it]?.aggregateBy(by)
                    if (aggregateBy != null) {
                        val aggregatedData = aggregateBy[0]
                        val stationDetails = (StationInfoService().getStationDetails(aggregatedData.station_number)).await().data
                        val geometry = stationDetails.geometry
                        val whereMeasured = stationDetails.location
                        mutex.withLock {
                            firstResult.add(ExtData(aggregatedData.formula,
                                    aggregatedData.station_number,
                                    aggregatedData.timestamp_measured,
                                    aggregatedData.value,
                                    by.name,
                                    whereMeasured,
                                    geometry.coordinates))
                        }
                    }
                }
            }.awaitAll()
            response.write(Json.encode(firstResult))
            response.endAwait()
        } finally {
            firstResult.clear()
        }
    }
}