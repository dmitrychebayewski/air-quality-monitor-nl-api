package com.minskrotterdam.airquality.routes

import com.minskrotterdam.airquality.common.API_ENDPOINT
import com.minskrotterdam.airquality.common.coroutineHandler
import com.minskrotterdam.airquality.handlers.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class TestRoutes(private val vertx: Vertx) {
    fun createRouter(): Router {
        val configHandlers = ConfigHandlers()

        return Router.router(vertx).apply {
            route().handler(configHandlers.corsHandler)
            route().handler(configHandlers.bodyHandler)
            get("$API_ENDPOINT/stations/:location").coroutineHandler { StationsHandler().stationsHandler(it) }
            get("$API_ENDPOINT/components").coroutineHandler { ComponentsHandler().pollutantComponentsHandler(it) }
            get("$API_ENDPOINT/components/:formula").coroutineHandler { ComponentInfoHandler().pollutantComponentInfoHandler(it) }
            get("$API_ENDPOINT/measurements").coroutineHandler { MeasurementsHandler().airMeasurementsHandler(it) }
            get("$API_ENDPOINT/measurements/:station_number").coroutineHandler { StationMeasurementsHandler().airMeasurementsHandler(it) }
        }
    }
}