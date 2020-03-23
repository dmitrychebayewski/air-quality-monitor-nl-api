package com.minskrotterdam.airquality.services

import com.minskrotterdam.airquality.environment.TEST_PORT
import com.minskrotterdam.airquality.verticles.TestVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.net.ServerSocket


abstract class AbstractHttpServiceIT {
    private var vertX: Vertx = Vertx.vertx()
    protected var port: Int = TEST_PORT
    private val httpClient: CloseableHttpClient = HttpClients.createDefault()

    protected fun setupVerticle(context: TestContext) {
        vertX = Vertx.vertx()
        val socket = ServerSocket(0)
        port = socket.localPort
        socket.close()
        val options = DeploymentOptions()
                .setConfig(JsonObject().put("http.port", port)
                )
        vertX.deployVerticle(TestVerticle::class.java.name, options, context.asyncAssertSuccess())
    }


    protected fun httpGet(uri: String): CloseableHttpResponse {
        val httpGet = HttpGet(uri)
        return httpClient.execute(httpGet)
    }

    protected fun tearDownTests(ctx: TestContext) {
        vertX.close(ctx.asyncAssertSuccess())
    }
}