package com.minskrotterdam.airquality.verticles

import com.minskrotterdam.airquality.config.PORT
import com.minskrotterdam.airquality.handlers.onetime.CacheHandler
import com.minskrotterdam.airquality.handlers.periodic.PeriodicAggregatedComponentsMeasurementHandler
import com.minskrotterdam.airquality.handlers.periodic.PeriodicAggregatedMeasurementsHandler
import com.minskrotterdam.airquality.routes.Routes
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit


class MainVerticle : CoroutineVerticle() {
    private val top7Request = MultiMap.caseInsensitiveMultiMap()

    override suspend fun start() {
        withContext(Dispatchers.Default) {
            CacheHandler().initStationsCache()
        }
        val vertx = Vertx.vertx()
        val router = Routes(vertx).createRouter()
        LoggerFactory.getLogger("VertxServer")
        vertx.createHttpServer()
        awaitResult<HttpServer> {
            vertx.createHttpServer()
                    .requestHandler(router::handle)
                    .listen(PORT, it)
        }
        scheduleTop7DataRefresh(vertx)
        scheduleRegioRdDataRefresh(vertx, "ZH")
        scheduleRegioRdDataRefresh(vertx, "RD")

    }

    private fun scheduleTop7DataRefresh(vertx: Vertx) {
        top7Request.add("formula", "CO")
        top7Request.add("formula", "NO")
        top7Request.add("formula", "NO2")
        top7Request.add("formula", "PM10")
        top7Request.add("formula", "PM25")
        top7Request.add("formula", "PM25")
        top7Request.add("formula", "O3")
        top7Request.add("formula", "SO2")
        top7Request["aggr"] = "max"
        vertx.setPeriodic(3630000) {
            val logger = LoggerFactory.getLogger("VertxServer")
            launch {
                val now = Instant.now()
                val end = now.truncatedTo(ChronoUnit.SECONDS).toString()
                val start = now.truncatedTo(ChronoUnit.SECONDS).minus(2, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES).toString()
                top7Request["start"] = start
                top7Request["end"] = end
                PeriodicAggregatedComponentsMeasurementHandler(vertx).aggregatedComponentsMeasurementHandler(top7Request)
            }
            val top7 = vertx.sharedData().getLocalMap<String, String>("top7")["results"]
            logger.info("Stations measurements: $top7")
        }
    }

    private fun scheduleRegioRdDataRefresh(vertx: Vertx, region: String) {
        vertx.setPeriodic(3600000) {
            val logger = LoggerFactory.getLogger("VertxServer")
            val componentsList = listOf("FN", "NO", "NO2", "PM10", "PM25")
            launch {
                val job = Job()
                componentsList.map {
                    CoroutineScope(Dispatchers.IO + job).async {
                        val now = Instant.now()
                        val end = now.truncatedTo(ChronoUnit.SECONDS).toString()
                        val start = now.truncatedTo(ChronoUnit.SECONDS).minus(2, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES).toString()
                        val regioRequest = MultiMap.caseInsensitiveMultiMap()
                        regioRequest["aggr"] = "avg"
                        regioRequest["start"] = start
                        regioRequest["end"] = end
                        regioRequest["formula"] = it
                        PeriodicAggregatedMeasurementsHandler(vertx).aggregatedAirMeasurementsHandler(regioRequest, region, null)
                    }
                }
            }
            componentsList.map {
                val data = vertx.sharedData().getLocalMap<String, String>(region)[it]
                logger.info("$region $it: $data")
            }
        }
    }
}
