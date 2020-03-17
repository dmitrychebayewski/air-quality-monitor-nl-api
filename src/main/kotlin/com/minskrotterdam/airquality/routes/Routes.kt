package com.minskrotterdam.airquality.routes

import com.minskrotterdam.airquality.common.API_ENDPOINT
import com.minskrotterdam.airquality.common.coroutineHandler
import com.minskrotterdam.airquality.handlers.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class Routes(private val vertx: Vertx) {
    fun createRouter(): Router {
        val configHandlers = ConfigHandlers()

        return Router.router(vertx).apply {
            // CONFIG ROUTES
            route().handler(configHandlers.corsHandler)
            route().handler(configHandlers.bodyHandler)
            //Air stations route
            get("$API_ENDPOINT/stations/:location").coroutineHandler { StationsHandler().stationsHandler(it) }
            //Air Pollutant Route
            get("$API_ENDPOINT/components").coroutineHandler { ComponentsHandler().pollutantComponentsHandler(it) }
            // Measurements Route
            get("$API_ENDPOINT/measurements").coroutineHandler { MeasurementsHandler().airMeasurementsHandler(it) }
            // Station Measurements Route
            get("$API_ENDPOINT/measurements/:station_number").coroutineHandler { StationMeasurementsHandler().airMeasurementsHandler(it) }
            route("/public/*").handler(configHandlers.staticHandler)
            route().handler { configHandlers.otherPageHandler(it) }
        }
    }
}