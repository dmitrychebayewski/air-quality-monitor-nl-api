package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.verticles.TestVerticle
import io.vertx.core.Vertx
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

abstract class AbstractHttpServiceIT {
    private val vertX: Vertx = Vertx.vertx()
    private val httpClient: CloseableHttpClient = HttpClients.createDefault()

    protected fun setupTests() {
        vertX.deployVerticle(TestVerticle()) { ar ->
            if (ar.succeeded()) {
                println("Application started")
            } else {
                println("Could not start application")
                ar.cause().printStackTrace()
            }
        }
    }


    protected fun httpGet(uri: String): CloseableHttpResponse {
        val httpGet = HttpGet(uri)
        return httpClient.execute(httpGet)
    }

    protected fun tearDownTests() {
        vertX.close()
    }
}