package com.minskrotterdam.airquality.routes

import com.minskrotterdam.airquality.config.API_ENDPOINT
import com.minskrotterdam.airquality.extensions.coroutineHandler
import com.minskrotterdam.airquality.handlers.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

val STATIONS_PATH = "$API_ENDPOINT/stations"
val STATION_PATH = "$API_ENDPOINT/station"

val COMPONENTS_PATH = "$API_ENDPOINT/components"
val COMPONENT_FORMULA_PATH = "$API_ENDPOINT/component/info"
val COMPONENT_FORMULA_LIMIT_PATH = "$API_ENDPOINT/component/limit"

val MEASUREMENTS_PATH = "$API_ENDPOINT/measurements"
val MEASUREMENT_STATION_PATH = "$API_ENDPOINT/measurement/station"
val MEASUREMENT_REGION_PATH = "$API_ENDPOINT/measurement/region"


class Routes(private val vertx: Vertx) {
    fun createRouter(): Router {
        val configHandlers = ConfigHandlers()

        return Router.router(vertx).apply {
            route().handler(configHandlers.corsHandler)
            route().handler(configHandlers.bodyHandler)
            get("$STATIONS_PATH/:location").coroutineHandler { StationsHandler().stationsHandler(it) }
            get("$STATION_PATH/:station_number").coroutineHandler { StationInformationHandler().stationInformationHandler(it) }
            get(COMPONENTS_PATH).coroutineHandler { ComponentsHandler().pollutantComponentsHandler(it) }
            get("$COMPONENT_FORMULA_PATH/:formula").coroutineHandler { ComponentInformationHandler().pollutantComponentInfoHandler(it) }
            get("$COMPONENT_FORMULA_LIMIT_PATH/:formula").coroutineHandler { ComponentInformationHandler().pollutantComponentLimitHandler(it) }
            get(MEASUREMENTS_PATH).coroutineHandler { MeasurementsHandler().airMeasurementsHandler(it) }
            get("$MEASUREMENT_STATION_PATH/:station_number").coroutineHandler { AggregatedMeasurementsHandler().airMeasurementsHandler(it) }
            get("$MEASUREMENT_REGION_PATH/:region").coroutineHandler { AggregatedMeasurementsHandler().aggregatedAirMeasurementsHandler(it) }
            route("/public/*").handler(configHandlers.staticHandler)
            route().handler { configHandlers.otherPageHandler(it) }
        }
    }
}