package com.minskrotterdam.airquality.handlers.periodic

import com.minskrotterdam.airquality.handlers.BaseAggregatedMeasurementsHandler
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PeriodicAggregatedMeasurementsHandler(vertx: Vertx) : BaseAggregatedMeasurementsHandler(vertx) {

    suspend fun aggregatedAirMeasurementsHandler(requestParameters: MultiMap, regio: String, stationId: String?) {
        getAggregatedMeasurements(requestParameters, regio, stationId)
    }

    private suspend fun getAggregatedMeasurements(requestParameters: MultiMap, regio: String, stationId: String?) {
        val result = get(requestParameters, regio, stationId)
        val formula = requestParameters.get("formula")
        val mutex = Mutex()
        val localMap = regio(regio)
        mutex.withLock(localMap) {
            localMap[formula] = result
        }
    }
}