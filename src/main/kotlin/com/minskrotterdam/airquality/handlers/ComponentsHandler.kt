package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.getSafeLaunchRanges
import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.services.ComponentsService
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.gildor.coroutines.retrofit.await

class ComponentsHandler {
    private val logger = LoggerFactory.getLogger("VertxServer")

    suspend fun pollutantComponentsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getPollutants(ctx)
        }
    }

    private suspend fun getPollutants(ctx: RoutingContext) {
        val response = ctx.response()
        response.isChunked = true
        val firstPage = ComponentsService().getPollutants(1).await()
        val pagination = firstPage.pagination
        val mutex = Mutex()
        mutex.withLock {
            response.write("[")
            val groupBy = firstPage.data.groupBy { it.formula }
            response.write(Json.encode(groupBy))
        }
        getSafeLaunchRanges(pagination.last_page).forEach {
            it.map {
                val job = Job()
                CoroutineScope(Dispatchers.Default + job).async {
                    val measurement = ComponentsService().getPollutants(it).await()
                    mutex.withLock {
                        response.write(",")
                        response.write(Json.encode(measurement.data.groupBy { it.formula }))
                    }
                }
            }.awaitAll()
        }
        mutex.withLock {
            response.write("]")
            response.endAwait()
        }
    }
}
