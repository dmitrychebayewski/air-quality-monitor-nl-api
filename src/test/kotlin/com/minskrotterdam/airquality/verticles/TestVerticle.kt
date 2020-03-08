package com.minskrotterdam.airquality.verticles

import com.minskrotterdam.airquality.environment.TEST_PORT
import com.minskrotterdam.airquality.routes.TestRoutes
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx

class TestVerticle : AbstractVerticle() {
    override fun start() {
        val vertx = Vertx.vertx()
        val router = TestRoutes(vertx).createRouter()
        vertx.createHttpServer()
                .requestHandler(router::handle)
                .listen(TEST_PORT)

    }

}