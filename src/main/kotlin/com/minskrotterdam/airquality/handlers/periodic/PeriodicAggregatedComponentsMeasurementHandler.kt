package com.minskrotterdam.airquality.handlers.periodic

import com.minskrotterdam.airquality.handlers.BaseAggregatedComponentsMeasurementHandler
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PeriodicAggregatedComponentsMeasurementHandler(vertx: Vertx) : BaseAggregatedComponentsMeasurementHandler(vertx) {

    suspend fun aggregatedComponentsMeasurementHandler(requestParams: MultiMap) {
        getAggregatedComponentsMeasurement(requestParams)
    }

    private suspend fun getAggregatedComponentsMeasurement(requestParams: MultiMap) {
        val result = get(requestParams)
        val mutex = Mutex()
        val localMap = top7()
        mutex.withLock(localMap) {
            localMap["results"] = result
        }
    }
}