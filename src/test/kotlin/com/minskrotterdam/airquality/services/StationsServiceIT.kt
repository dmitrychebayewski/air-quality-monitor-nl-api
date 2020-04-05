package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.stations.Coordinates
import com.minskrotterdam.airquality.models.stations.Data
import com.minskrotterdam.airquality.routes.STATIONS_COORDINATES_PATH
import com.minskrotterdam.airquality.routes.STATIONS_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.io.ByteArrayOutputStream

@RunWith(VertxUnitRunner::class)
class StationsServiceIT : AbstractHttpServiceIT() {
    val LOCATION = "amsterdam"
    val PLEINWEG_LOCATION = "Rotterdam-Pleinweg"
    val ZuiderterrasCoordinates = Coordinates(4.4883894, 51.8848629)

    private fun stationsUrl(): String {
        return "${TEST_API_URL}:${port}/${STATIONS_PATH}/amsterdam"
    }

    private fun urlNearestToZuiderterras(): String {
        return "${TEST_API_URL}:${port}/${STATIONS_COORDINATES_PATH}?lng=${ZuiderterrasCoordinates.lng}&lat=${ZuiderterrasCoordinates.lat}"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(stationsUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
        ctx.assertEquals(response.getFirstHeader("content-type").value, "application/json")
    }

    @Test
    fun testIsGivingValidData(ctx: TestContext) {
        val entity = httpGet(stationsUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val stations = Gson().fromJson(content.toString(), Array<Data>::class.java)
        stations.forEach { station -> ctx.assertTrue(station.location.toLowerCase().contains(LOCATION)) }
    }

//    @Test
//    fun testIsGivingValidLocation(ctx: TestContext) {
//        val entity = httpGet(urlNearestToZuiderterras()).entity
//        val content = ByteArrayOutputStream()
//        entity.writeTo(content)
//        println(content)
//        val stations = Gson().fromJson(content.toString(), Array<Data>::class.java)
//        stations.forEach { station -> ctx.assertEquals(station.location, PLEINWEG_LOCATION) }
//    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}