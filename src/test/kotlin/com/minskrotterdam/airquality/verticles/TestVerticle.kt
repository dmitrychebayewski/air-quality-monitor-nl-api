package com.minskrotterdam.airquality.verticles

import com.minskrotterdam.airquality.environment.TEST_PORT
import com.minskrotterdam.airquality.routes.Routes
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx

class TestVerticle : AbstractVerticle() {

    override fun start() {
        val vertx = Vertx.vertx()
        val router = Routes(vertx).createRouter()
        vertx.createHttpServer()
                .requestHandler(router::handle)
                .listen(config().getInteger("http.port", TEST_PORT))
    }
}