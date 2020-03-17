package com.minskrotterdam.airquality.routes

import com.minskrotterdam.airquality.common.API_ENDPOINT
import com.minskrotterdam.airquality.common.coroutineHandler
import com.minskrotterdam.airquality.handlers.MeasurementsHandler
import com.minskrotterdam.airquality.handlers.ConfigHandlers
import com.minskrotterdam.airquality.handlers.ComponentsHandler
import com.minskrotterdam.airquality.handlers.StationsHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class TestRoutes(private val vertx: Vertx) {
    fun createRouter(): Router {
        val configHandlers = ConfigHandlers()

        return Router.router(vertx).apply {
            // CONFIG ROUTES
            route().handler(configHandlers.corsHandler)
            route().handler(configHandlers.bodyHandler)
            //Air stations route
            get("$API_ENDPOINT/stations/:location").coroutineHandler { StationsHandler().stationsHandler(it) }
            //Air pollutant components route
            get("$API_ENDPOINT/components").coroutineHandler { ComponentsHandler().pollutantComponentsHandler(it) }
            //Air quality measurements route
            get("$API_ENDPOINT/measurements").coroutineHandler { MeasurementsHandler().airMeasurementsHandler(it) }
            // route().handler { configHandlers.otherPageHandler(it) }
        }
    }
}