package com.minskrotterdam.airquality

import com.minskrotterdam.airquality.verticles.MainVerticle
import io.vertx.core.Vertx

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle()) { ar ->
        if (ar.succeeded()) {
            println("Application started")
        } else {
            println("Could not start application")
            ar.cause().printStackTrace()
        }
    }
}