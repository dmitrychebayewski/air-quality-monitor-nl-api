package com.minskrotterdam.airquality.verticles

import com.minskrotterdam.airquality.config.PORT
import com.minskrotterdam.airquality.routes.Routes
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.LoggerFactory

class MainVerticle : CoroutineVerticle() {

    override suspend fun start() {
        val vertx = Vertx.vertx()
        val router = Routes(vertx).createRouter()
        LoggerFactory.getLogger("VertxServer")

        awaitResult<HttpServer> {
            vertx.createHttpServer()
                    .requestHandler(router::handle)
                    .listen(PORT, it)
        }
    }

}
