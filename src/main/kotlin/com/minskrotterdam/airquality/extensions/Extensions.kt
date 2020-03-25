package com.minskrotterdam.airquality.extensions

import com.minskrotterdam.airquality.models.CommonServiceError
import com.minskrotterdam.airquality.models.measurements.Data
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun HttpServerResponse.endWithJson(obj: Any?, statusCode: Int = 200) {
    setStatusCode(statusCode)
    putHeader("Content-Type", "application/json; charset=utf-8").end(Json.encodePrettily(obj))
}

fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        GlobalScope.launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}

fun <T> safeLaunch(ctx: RoutingContext, body: suspend () -> T) {
    GlobalScope.launch(ctx.vertx().dispatcher()) {
        try {
            body()
        } catch (e: Exception) {
            ctx.response().endWithJson(CommonServiceError(e.localizedMessage, 501))
        }
    }
}

fun getSafeLaunchRanges(pages: Int): List<IntRange> {
    val limitOfConcurrentLaunches = 30
    val numberOfRanges = (pages - 1) / limitOfConcurrentLaunches
    val remainder = (pages - 1) % limitOfConcurrentLaunches
    var low = 2
    var hi = if (numberOfRanges == 0) low + remainder - 1 else low + limitOfConcurrentLaunches - 1
    val subSet = mutableListOf<IntRange>()
    subSet.checkAndInclude(low..hi)
    (1..numberOfRanges).forEach { _ ->
        low += limitOfConcurrentLaunches
        hi = low + limitOfConcurrentLaunches - 1
        if (hi > pages) {
            hi = low + remainder - 1
        }
        subSet.checkAndInclude(low..hi)
    }
    return subSet
}

private fun MutableList<IntRange>.checkAndInclude(range: IntRange) {
    if (range.first <= range.last) add(range)
}


